package demo.model;

public class PackageData {
    private final String path;
    private final String description;
    private final String size;
 
    public PackageData(String path) {
    	this(path, null);
    }
    
    public PackageData(String path, String description) {
    	this(path, description, null);
    }
    
    public PackageData(String path, String description, String size) {
        this.path = path;
        this.description = description;
        this.size = size;
    }
 
    public String getPath() {
        return path;
    }
 
    public String getDescription() {
        return description;
    }
    
    public String getSize() {
    	return size;
    }
 
}