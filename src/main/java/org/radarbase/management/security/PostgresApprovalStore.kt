/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.radarbase.management.security

import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.security.oauth2.provider.approval.Approval
import org.springframework.security.oauth2.provider.approval.Approval.ApprovalStatus
import org.springframework.security.oauth2.provider.approval.ApprovalStore
import org.springframework.util.Assert
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.util.*
import javax.sql.DataSource

/**
 * This class will be used to execute functions related to token approval. It is an duplicate of
 * JdbcApprovalStore with escaped case sensitive fields to query.
 *
 * @author Dave Syer
 * @author Modified by Nivethika
 */
class PostgresApprovalStore(dataSource: DataSource?) : ApprovalStore {
    private val jdbcTemplate: JdbcTemplate
    private val rowMapper: RowMapper<Approval> = AuthorizationRowMapper()
    private var addApprovalStatement = DEFAULT_ADD_APPROVAL_STATEMENT
    private var refreshApprovalStatement = DEFAULT_REFRESH_APPROVAL_STATEMENT
    private var findApprovalStatement = DEFAULT_GET_APPROVAL_SQL
    private var deleteApprovalStatment = DEFAULT_DELETE_APPROVAL_SQL
    private var expireApprovalStatement = DEFAULT_EXPIRE_APPROVAL_STATEMENT
    private var handleRevocationsAsExpiry = false

    init {
        Assert.notNull(dataSource)
        jdbcTemplate = JdbcTemplate(dataSource)
    }

    fun setHandleRevocationsAsExpiry(handleRevocationsAsExpiry: Boolean) {
        this.handleRevocationsAsExpiry = handleRevocationsAsExpiry
    }

    fun setAddApprovalStatement(addApprovalStatement: String) {
        this.addApprovalStatement = addApprovalStatement
    }

    fun setFindApprovalStatement(findApprovalStatement: String) {
        this.findApprovalStatement = findApprovalStatement
    }

    fun setDeleteApprovalStatment(deleteApprovalStatment: String) {
        this.deleteApprovalStatment = deleteApprovalStatment
    }

    fun setExpireApprovalStatement(expireApprovalStatement: String) {
        this.expireApprovalStatement = expireApprovalStatement
    }

    fun setRefreshApprovalStatement(refreshApprovalStatement: String) {
        this.refreshApprovalStatement = refreshApprovalStatement
    }

    override fun addApprovals(approvals: Collection<Approval>): Boolean {
        logger.debug(String.format("adding approvals: [%s]", approvals))
        var success = true
        for (approval in approvals) {
            if (!updateApproval(refreshApprovalStatement, approval) && !updateApproval(
                    addApprovalStatement, approval
                )
            ) {
                success = false
            }
        }
        return success
    }

    override fun revokeApprovals(approvals: Collection<Approval>): Boolean {
        logger.debug(String.format("Revoking approvals: [%s]", approvals))
        var success = true
        for (approval in approvals) {
            if (handleRevocationsAsExpiry) {
                val refreshed = jdbcTemplate
                    .update(expireApprovalStatement) { ps: PreparedStatement ->
                        ps.setTimestamp(1, Timestamp(System.currentTimeMillis()))
                        ps.setString(2, approval.userId)
                        ps.setString(3, approval.clientId)
                        ps.setString(4, approval.scope)
                    }
                if (refreshed != 1) {
                    success = false
                }
            } else {
                val refreshed = jdbcTemplate
                    .update(deleteApprovalStatment) { ps: PreparedStatement ->
                        ps.setString(1, approval.userId)
                        ps.setString(2, approval.clientId)
                        ps.setString(3, approval.scope)
                    }
                if (refreshed != 1) {
                    success = false
                }
            }
        }
        return success
    }

    /**
     * Purges expired approvals from database.
     * @return `true` if removed successfully, `false` otherwise.
     */
    fun purgeExpiredApprovals(): Boolean {
        logger.debug("Purging expired approvals from database")
        try {
            val deleted = jdbcTemplate.update(deleteApprovalStatment) { ps: PreparedStatement ->
                ps.setTimestamp(
                    1, Timestamp(
                        Date().time
                    )
                )
            }
            logger.debug("$deleted expired approvals deleted")
        } catch (ex: DataAccessException) {
            logger.error("Error purging expired approvals", ex)
            return false
        }
        return true
    }

    override fun getApprovals(userName: String, clientId: String): List<Approval> {
        logger.debug("Finding approvals for userName {} and cliendId {}", userName, clientId)
        return jdbcTemplate.query(findApprovalStatement, rowMapper, userName, clientId)
    }

    private fun updateApproval(sql: String, approval: Approval): Boolean {
        logger.debug(String.format("refreshing approval: [%s]", approval))
        val refreshed = jdbcTemplate.update(sql) { ps: PreparedStatement ->
            ps.setTimestamp(1, Timestamp(approval.expiresAt.time))
            ps.setString(2, (if (approval.status == null) ApprovalStatus.APPROVED else approval.status).toString())
            ps.setTimestamp(3, Timestamp(approval.lastUpdatedAt.time))
            ps.setString(4, approval.userId)
            ps.setString(5, approval.clientId)
            ps.setString(6, approval.scope)
        }
        return refreshed == 1
    }

    private class AuthorizationRowMapper : RowMapper<Approval> {
        @Throws(SQLException::class)
        override fun mapRow(rs: ResultSet, rowNum: Int): Approval {
            val userName = rs.getString(4)
            val clientId = rs.getString(5)
            val scope = rs.getString(6)
            val expiresAt: Date = rs.getTimestamp(1)
            val status = rs.getString(2)
            val lastUpdatedAt: Date = rs.getTimestamp(3)
            return Approval(
                userName, clientId, scope, expiresAt,
                ApprovalStatus.valueOf(status), lastUpdatedAt
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PostgresApprovalStore::class.java)
        private const val TABLE_NAME = "oauth_approvals"
        private const val FIELDS = ("\"expiresAt\", \"status\",\"lastModifiedAt\",\"userId\"," + "\"clientId\","
                + "\"scope\"")
        private const val WHERE_KEY = "where \"userId\"=? and \"clientId\"=?"
        private const val WHERE_KEY_AND_SCOPE = WHERE_KEY + " and \"scope\"=?"
        private const val AND_LESS_THAN_EXPIRE_AT = " and \"expiresAt\" <= ?"
        private val DEFAULT_ADD_APPROVAL_STATEMENT =
            String.format("insert into %s ( %s ) values (?,?,?,?,?,?)", TABLE_NAME, FIELDS)
        private val DEFAULT_REFRESH_APPROVAL_STATEMENT = String.format(
            "update %s set \"expiresAt\"=?, \"status\"=?, \"lastModifiedAt\"=? "
                    + WHERE_KEY_AND_SCOPE, TABLE_NAME
        )
        private val DEFAULT_GET_APPROVAL_SQL = String.format("select %s from %s " + WHERE_KEY, FIELDS, TABLE_NAME)
        private val DEFAULT_DELETE_APPROVAL_SQL = String.format(
            "delete from %s " + WHERE_KEY_AND_SCOPE + AND_LESS_THAN_EXPIRE_AT,
            TABLE_NAME
        )
        private val DEFAULT_EXPIRE_APPROVAL_STATEMENT = String.format(
            "update %s set " + "\"expiresAt\" = ? "
                    + WHERE_KEY_AND_SCOPE, TABLE_NAME
        )
    }
}
