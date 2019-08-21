export class OAuthClient {

    public clientId: string;
    public clientSecret?: string;
    public scope?: string[];
    public resourceIds?: string[];
    public authorizedGrantTypes?: string[];
    public autoApproveScopes?: string[];
    public accessTokenValiditySeconds?: number;
    public refreshTokenValiditySeconds?: number;
    public authorities?: string[];
    public additionalInformation?: any;
    public registeredRedirectUri?: string[];

    constructor(
            clientId?: string,
            clientSecret?: string,
            scope?: string[],
            resourceIds?: string[],
            authorizedGrantTypes?: string[],
            autoApproveScopes?: string[],
            accessTokenValiditySeconds?: number,
            refreshTokenValiditySeconds?: number,
            authorities?: string[],
            additionalInformation?: any,
            registeredRedirectUri?: string[]
    ) {
        this.clientId = clientId ? clientId : '';
        this.clientSecret = clientSecret ? clientSecret : '';
        this.scope = scope ? scope : [];
        this.resourceIds = resourceIds ? resourceIds : [];
        this.authorizedGrantTypes = authorizedGrantTypes ? authorizedGrantTypes : [];
        this.autoApproveScopes = autoApproveScopes ? autoApproveScopes : [];
        this.accessTokenValiditySeconds = accessTokenValiditySeconds ? accessTokenValiditySeconds : 0;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds ? refreshTokenValiditySeconds : 0;
        this.authorities = authorities ? authorities : [];
        this.additionalInformation = additionalInformation ? additionalInformation : {};
        this.registeredRedirectUri = registeredRedirectUri ? registeredRedirectUri : [];
    }
}
