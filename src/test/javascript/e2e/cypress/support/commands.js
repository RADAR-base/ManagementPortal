export const APP_URL = "http://localhost:4433"

export const email = () => Math.random().toString(36) + "@ory.sh"
export const password = () => Math.random().toString(36)

export const gen = {
    email,
    password,
    identity: () => ({ email: email(), password: password() }),
    identityWithWebsite: () => ({
      email: email(),
      password: password()
    }),
  }

const mergeFields = (form, fields) => {
    const result = {}
    form.nodes.forEach(({ attributes, type }) => {
      if (type === "input") {
        result[attributes.name] = attributes.value
      }
    })
  
    return { ...result, ...fields }
  }

  Cypress.Commands.add('createUser', (email, password) => {
    return cy.request({
      method: 'POST',
      url: APP_URL + '/admin/identities',
      body: {
        schema_id: 'user',
        traits: {
          email: email,
          name: 'Test User'
        },
        credentials: {
          password: {
            config: {
              password: password
            }
          }
        }
      },
      headers: {
        'Content-Type': 'application/json'
      }
    });
  });

Cypress.Commands.add("submitPasswordForm", () => {
    cy.get('[name="method"][value="password"]').click()
    cy.get('[name="method"][value="password"]:disabled').should("not.exist")
  })

module.exports = { gen };
