package com.usmani.android;

public class MediaEntity {
	private long id;
	private String caption;
	private String path;
	
	private MediaType type;
	
	public MediaEntity(){		
	}
	
	public MediaType getMediaType(){
		return type;
	}
	
	public void setMediaType(MediaType type){
		this.type=type;
	}
	
	public long getId(){
		return id;
	}
	
	public void setId(long i){
		id=i;
	}
		
	public String getCaption(){
		return caption;
	}
	
	public void setCaption(String cpt){
		caption=cpt;
	}
	
	public String getPath(){
		return path;
	}
	
	public void setPath(String p){
		path=p;
	}
}
