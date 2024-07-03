export const IDENTITY_SERVER = 'http://localhost:4433'

Cypress.Commands.add('initiateLoginFlow', () => {
  return cy.request({
    method: 'GET',
    url: IDENTITY_SERVER + '/self-service/login/browser',
    followRedirect: false
  }).then((response) => {
    const redirectUrl = new URL(response.headers['location']);
    const flow = redirectUrl.searchParams.get('flow');
    return cy.request({
      method: 'GET',
      url: redirectUrl.href
    }).then((htmlResponse) => {
      const csrfToken = Cypress.$(htmlResponse.body).find('input[name="csrf_token"]').val();
      return { flow, csrfToken };
    });
  });
});

Cypress.Commands.add('getCSRFToken', () => {
  return cy.get('input[name="csrf_token"]').then((input) => input.val());
});

Cypress.Commands.add('login', (identifier, password) => {
  cy.initiateLoginFlow().then(({flow, csrfToken}) => {
      cy.request({
        method: 'POST',
        url: IDENTITY_SERVER + `/self-service/login?flow=${flow}`,
        body: {
          identifier: identifier,
          password: password,
          csrf_token: csrfToken,
          method: 'password'
        },
        headers: {
          'Content-Type': 'application/json'
        }
      }).then((response) => {
        const cookies = response.headers['set-cookie'] || [];
        cookies.forEach((cookie) => {
          const [cookieName, cookieValue] = cookie.split(';')[0].split('=');
          cy.setCookie(cookieName, cookieValue);
          Cypress.Cookies.defaults({
            preserve: [cookieName, cookieValue] // Ensure relevant cookies are preserved
          });
        });
      });
  });
});
