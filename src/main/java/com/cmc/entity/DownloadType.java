package com.cmc.entity;

public enum DownloadType {
    PDF_DOWNLOAD("PDF Download"),
    IMAGE_VIEW("Image View");
    
    private final String displayName;
    
    DownloadType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
