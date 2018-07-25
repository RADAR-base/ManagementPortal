package org.radarcns.management.service;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.radarcns.management.ManagementPortalTestApp;
import org.radarcns.management.domain.MetaToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@Transactional
public class MetaTokenServiceTest {


    @Autowired
    private MetaTokenService metaTokenService;

    @Test
    public void testSaveMetaToken() {
        MetaToken token = new MetaToken();
        token.fetched(false);
        token.token("{\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJzdWIiOiJhZG1pbi"
                + "IsInNvdX"
                + "JjZXMiOltdLCJ1c2VyX25hbWUiOiJhZG1pbiIsInJvbGVzIjpbXSwiaXNzIjoiTWFuYWdlbWVudFB"
                + "vcnRhbCIsImF1dGhvcml0aWVzIjpbIlJPTEVfU1lTX0FETUlOIl0sImNsaWVudF9pZCI6Ik1hbmFn"
                + "ZW1lbnRQb3J0YWxhcHAiLCJhdWQiOiJyZXNfTWFuYWdlbWVudFBvcnRhbCIsImdyYW50X3R5cGUiO"
                + "iJwYXNzd29yZCIsInNjb3BlIjpbIlNPVVJDRVRZUEUuQ1JFQVRFIiwiU09VUkNFVFlQRS5SRUFEIi"
                + "wiU09VUkNFVFlQRS5VUERBVEUiLCJTT1VSQ0VUWVBFLkRFTEVURSIsIlNPVVJDRURBVEEuQ1JFQVR"
                + "FIiwiU09VUkNFREFUQS5SRUFEIiwiU09VUkNFREFUQS5VUERBVEUiLCJTT1VSQ0VEQVRBLkRFTEVU"
                + "RSIsIlNPVVJDRS5DUkVBVEUiLCJTT1VSQ0UuUkVBRCIsIlNPVVJDRS5VUERBVEUiLCJTT1VSQ0UuR"
                + "EVMRVRFIiwiU1VCSkVDVC5DUkVBVEUiLCJTVUJKRUNULlJFQUQiLCJTVUJKRUNULlVQREFURSIsIl"
                + "NVQkpFQ1QuREVMRVRFIiwiVVNFUi5DUkVBVEUiLCJVU0VSLlJFQUQiLCJVU0VSLlVQREFURSIsIlV"
                + "TRVIuREVMRVRFIiwiUk9MRS5DUkVBVEUiLCJST0xFLlJFQUQiLCJST0xFLlVQREFURSIsIlJPTEUu"
                + "REVMRVRFIiwiUFJPSkVDVC5DUkVBVEUiLCJQUk9KRUNULlJFQUQiLCJQUk9KRUNULlVQREFURSIsI"
                + "lBST0pFQ1QuREVMRVRFIiwiT0FVVEhDTElFTlRTLkNSRUFURSIsIk9BVVRIQ0xJRU5UUy5SRUFEIi"
                + "wiT0FVVEhDTElFTlRTLlVQREFURSIsIk9BVVRIQ0xJRU5UUy5ERUxFVEUiLCJBVURJVC5DUkVBVEU"
                + "iLCJBVURJVC5SRUFEIiwiQVVESVQuVVBEQVRFIiwiQVVESVQuREVMRVRFIiwiQVVUSE9SSVRZLkNS"
                + "RUFURSIsIkFVVEhPUklUWS5SRUFEIiwiQVVUSE9SSVRZLlVQREFURSIsIkFVVEhPUklUWS5ERUxFV"
                + "EUiLCJNRUFTVVJFTUVOVC5DUkVBVEUiLCJNRUFTVVJFTUVOVC5SRUFEIiwiTUVBU1VSRU1FTlQuVV"
                + "BEQVRFIiwiTUVBU1VSRU1FTlQuREVMRVRFIl0sImV4cCI6MTUzMjQ1MjMyOCwiaWF0IjoxNTMyNDM"
                + "3OTI4LCJqdGkiOiJkY2E3MDQ3Yi00NjdlLTQ5OTEtOWY1Zi03N2NiMTA4MTA0YzQifQ.MEUCIGHhv"
                + "q-C9WRHAYecpgd3SM6ih2ejqwJ3Lp_qwsVd8o-mAiEAswpsnOoTi3qQC49y5hCFK3QODKt_9pJglF"
                + "DxwPqnG0A\",\"token_type\":\"bearer\",\"expires_in\":14399,\"scope\":\"SOURCE"
                + "TYPE.CREATE SOURCETYPE.READ SOURCETYPE.UPDATE SOURCETYPE.DELETE SOURCEDATA.CR"
                + "EATE SOURCEDATA.READ SOURCEDATA.UPDATE SOURCEDATA.DELETE SOURCE.CREATE SOURCE"
                + ".READ SOURCE.UPDATE SOURCE.DELETE SUBJECT.CREATE SUBJECT.READ SUBJECT.UPDATE "
                + "SUBJECT.DELETE USER.CREATE USER.READ USER.UPDATE USER.DELETE ROLE.CREATE ROLE"
                + ".READ ROLE.UPDATE ROLE.DELETE PROJECT.CREATE PROJECT.READ PROJECT.UPDATE PROJ"
                + "ECT.DELETE OAUTHCLIENTS.CREATE OAUTHCLIENTS.READ OAUTHCLIENTS.UPDATE OAUTHCLI"
                + "ENTS.DELETE AUDIT.CREATE AUDIT.READ AUDIT.UPDATE AUDIT.DELETE AUTHORITY.CREAT"
                + "E AUTHORITY.READ AUTHORITY.UPDATE AUTHORITY.DELETE MEASUREMENT.CREATE MEASURE"
                + "MENT.READ MEASUREMENT.UPDATE MEASUREMENT.DELETE\",\"sub\":\"admin\",\"sources"
                + "\":[],\"grant_type\":\"password\",\"roles\":[],\"iss\":\"ManagementPortal\","
                + "\"iat\":1532437928,\"jti\":\"dca7047b-467e-4991-9f5f-77cb108104c4\"}");
        MetaToken saved = metaTokenService.save(token);
        assertNotNull(saved.getId());
        assertNotNull(saved.getTokenName());

    }
}
