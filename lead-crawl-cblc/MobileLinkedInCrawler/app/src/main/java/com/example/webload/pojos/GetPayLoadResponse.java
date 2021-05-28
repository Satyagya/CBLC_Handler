package com.example.webload.pojos;

import java.util.List;

public class GetPayLoadResponse{

	private List<String> PROFILE_URL;
	private int REQUEST_ID;

	public List<String> getPROFILE_URL() {
		return PROFILE_URL;
	}

	public void setPROFILE_URL(List<String> PROFILE_URL) {
		this.PROFILE_URL = PROFILE_URL;
	}

	public int getREQUEST_ID() {
		return REQUEST_ID;
	}

	public void setREQUEST_ID(int REQUEST_ID) {
		this.REQUEST_ID = REQUEST_ID;
	}
}


