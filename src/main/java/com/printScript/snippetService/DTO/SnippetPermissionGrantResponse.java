package com.printScript.snippetService.DTO;

public class SnippetPermissionGrantResponse {
    private String snippetId;
    private String granteeId;
    private String permission;

    public SnippetPermissionGrantResponse() {}

    public SnippetPermissionGrantResponse(String snippetId, String granteeId, String permission) {
        this.snippetId = snippetId;
        this.granteeId = granteeId;
        this.permission = permission;
    }

    public String getSnippetId() { return snippetId; }
    public String getGranteeId() { return granteeId; }
    public String getPermission() { return permission; }

    public void setSnippetId(String snippetId) { this.snippetId = snippetId; }
    public void setGranteeId(String granteeId) { this.granteeId = granteeId; }
    public void setPermission(String permission) { this.permission = permission; }
}
