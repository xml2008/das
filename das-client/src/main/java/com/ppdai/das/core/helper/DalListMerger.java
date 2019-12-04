package com.ppdai.das.core.helper;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ComparisonChain;
import com.ppdai.das.client.sqlbuilder.ColumnOrder;
import com.ppdai.das.core.ResultMerger;


public class DalListMerger<T> implements ResultMerger<List<T>> {
	private List<T> result = new ArrayList<>();
	private Comparator<T> comparator;

	public DalListMerger() {
		this(null);
	}
	
	public DalListMerger(List<ColumnOrder> sorters) {
		if(sorters == null || sorters.isEmpty()) {
			return;
		}

		this.comparator = (o1, o2) ->{
			final Comparable least = (i) -> Integer.MIN_VALUE;
			ComparisonChain comparisonChain = ComparisonChain.start();
			for(ColumnOrder sorter: sorters){
				try {
					Object v1 = null;
					Object v2 = null;
					String colName = sorter.getColumn().getColumn().getColumnName();
					if (o1 instanceof Map || o2 instanceof Map) {//Map entity
						v1 = Optional.of(((Map)o1).get(colName)).orElse(least);
						v2 = Optional.of(((Map)o2).get(colName)).orElse(least);

					} else {//POJO entity
						Field f = o1.getClass().getDeclaredField(Introspector.decapitalize(colName));
						f.setAccessible(true);
						v1 = Optional.of(f.get(o1)).orElse(least);
						v2 = Optional.of(f.get(o2)).orElse(least);
					}
					if(sorter.isAsc()) {
						comparisonChain = comparisonChain.compare((Comparable)v1, (Comparable)v2);
					} else {
						comparisonChain = comparisonChain.compare((Comparable)v2, (Comparable)v1);
					}
				}catch (NoSuchFieldException |IllegalAccessException e){
					throw new RuntimeException(e);
				}
			}
			return comparisonChain.result();
		};
	}

	@Override
	public void addPartial(String shard, List<T> partial) {
		if(partial!=null)
			result.addAll(partial);
	}

	@Override
	public List<T> merge() {
		if(comparator != null) {
			Collections.sort(result, comparator);
		}

		return result;
	}
}