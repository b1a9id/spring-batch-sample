package com.example.springbatchsample.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.print.attribute.standard.OrientationRequested;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fruit {

	private String name;

	private int price;
}
