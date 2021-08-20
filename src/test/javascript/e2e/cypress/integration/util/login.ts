export function login(username = "admin", password = "admin") {
    return cy.request({
        url: "/oauthserver/oauth/token",
        method: "POST",
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `username=${username}&password=${password}&grant_type=password`
    }).then(res => {
        let token = res.body;
        const expiredAt = new Date();
        expiredAt.setSeconds(expiredAt.getSeconds() + token.expires_in);
        token.expires_at = expiredAt.getTime();
        cy.setCookie("oAtkn", JSON.stringify(token));
    });
}
