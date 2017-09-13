export class OAuthClient {

    public clientId: string;
    public clientSecret?: string;
    public scope?: string[];
    public resourceIds?: string[];
    public authorizedGrantTypes?: string[];
    public autoApprove?: boolean;
    public accessTokenValidity?: number;
    public refreshTokenValidity?: number;
    public authorities?: string[];

    constructor(
        clientId: string,
        clientSecret?: string,
        scope?: string[],
        resourceIds?: string[],
        authorizedGrantTypes?: string[],
        autoApprove?: boolean,
        accessTokenValidity?: number,
        refreshTokenValidity?: number,
        authorities?: string[]
    ) {
        this.clientSecret = clientSecret ? clientSecret : null,
        this.scope = scope ? scope : null,
        this.resourceIds = resourceIds ? resourceIds : null,
        this.authorizedGrantTypes = authorizedGrantTypes ? authorizedGrantTypes : null,
        this.autoApprove = autoApprove ? autoApprove : null,
        this.accessTokenValidity = accessTokenValidity ? accessTokenValidity : null,
        this.refreshTokenValidity = refreshTokenValidity ? refreshTokenValidity : null,
        this.authorities = authorities ? authorities : null
    }
}
