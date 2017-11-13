export class OAuthClient {

    public clientId: string;
    public clientSecret?: string;
    public scope?: string[];
    public resourceIds?: string[];
    public authorizedGrantTypes?: string[];
    public autoApproveScopes?: string[];
    public accessTokenValidity?: number;
    public refreshTokenValidity?: number;
    public authorities?: string[];
    public additionalInformation?: any;

    constructor(
        clientId?: string,
        clientSecret?: string,
        scope?: string[],
        resourceIds?: string[],
        authorizedGrantTypes?: string[],
        autoApproveScopes?: string[],
        accessTokenValidity?: number,
        refreshTokenValidity?: number,
        authorities?: string[],
        additionalInformation?: any
    ) {
        this.clientId = clientId ? clientId : '';
        this.clientSecret = clientSecret ? clientSecret : '',
        this.scope = scope ? scope : [],
        this.resourceIds = resourceIds ? resourceIds : [],
        this.authorizedGrantTypes = authorizedGrantTypes ? authorizedGrantTypes : [],
        this.autoApproveScopes = autoApproveScopes ? autoApproveScopes : [],
        this.accessTokenValidity = accessTokenValidity ? accessTokenValidity : 0,
        this.refreshTokenValidity = refreshTokenValidity ? refreshTokenValidity : 0,
        this.authorities = authorities ? authorities : [],
        this.additionalInformation = additionalInformation ? additionalInformation : {}
    }
}
