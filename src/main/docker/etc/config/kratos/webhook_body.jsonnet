function(ctx) {
    identity: if std.objectHas(ctx, "identity") then ctx.identity else null,
    payload: if std.objectHas(ctx, "flow") && std.objectHas(ctx.flow, "transient_payload") then ctx.flow.transient_payload else null,
    cookies: ctx.request_cookies
}
