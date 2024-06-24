export const APP_URL = "http://localhost:4433"

export const email = () => Math.random().toString(36) + "@ory.sh"
export const password = () => Math.random().toString(36)

export const gen = {
    email,
    password,
    identity: () => ({ email: email(), password: password() }),
    identityWithWebsite: () => ({
      email: email(),
      password: password(),
      fields: { "traits.website": "https://www.ory.sh" },
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

Cypress.Commands.add(
  "registerApi",
  ({ email = gen.email(), password = gen.password(), fields = {} } = {}) =>
    cy
      .request({
        url: APP_URL + "/self-service/registration/",
      })
      .then(({ body }) => {
        const form = body.ui
        return cy.request({
          method: form.method,
          body: mergeFields(form, {
            ...fields,
            "traits.email": email,
            password,
            method: "password",
          }),
          url: form.action,
        })
      })
      .then(({ body }) => {
        expect(body.identity.traits.email).to.contain(email)
        return body
      }),
)

Cypress.Commands.add("submitPasswordForm", () => {
    cy.get('[name="method"][value="password"]').click()
    cy.get('[name="method"][value="password"]:disabled').should("not.exist")
  })

module.exports = { gen };
