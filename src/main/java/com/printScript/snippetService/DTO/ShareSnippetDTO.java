package com.printScript.snippetService.DTO;

public class ShareSnippetDTO {
    private String snippetId;
    private String userId;
    private String permission;

    public ShareSnippetDTO() {}

    public ShareSnippetDTO(String snippetId, String userId, String permission) {
        this.snippetId = snippetId;
        this.userId = userId;
        this.permission = permission;
    }

    public String getSnippetId() { return snippetId; }
    public String getUserId() { return userId; }
    public String getPermission() { return permission; }

    public void setSnippetId(String snippetId) { this.snippetId = snippetId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setPermission(String permission) { this.permission = permission; }
}
