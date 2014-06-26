package com.example.locationtest;

import java.util.Comparator;

public class DistanceComparator implements Comparator<PhotoData>{

	@Override
	public int compare(PhotoData p1, PhotoData p2) {
		// TODO Auto-generated method stub
		  return  Double.compare(p1.distance, p2.distance);
	}
	

}
