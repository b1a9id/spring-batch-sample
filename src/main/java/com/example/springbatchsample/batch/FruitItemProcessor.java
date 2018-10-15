package com.example.springbatchsample.batch;

import com.example.springbatchsample.dto.Fruit;
import org.springframework.batch.item.ItemProcessor;

public class FruitItemProcessor implements ItemProcessor<Fruit, Fruit> {

	@Override
	public Fruit process(Fruit fruit) throws Exception {
		final String title = fruit.getName().toUpperCase();
		final int price = fruit.getPrice();

		final Fruit transformColumns = new Fruit(title, price);
		return transformColumns;
	}
}
