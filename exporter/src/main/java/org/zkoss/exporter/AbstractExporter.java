/* AbstractExporter.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 12, 2012 11:39:46 AM , Created by Sam
}}IS_NOTE

Copyright (C) 2012 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.exporter;

import static org.zkoss.exporter.util.Utils.invokeComponentGetter;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Group;
import org.zkoss.zul.Groupfoot;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listgroup;
import org.zkoss.zul.Listgroupfoot;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Row;
import org.zkoss.zul.Treecols;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.impl.MeshElement;

/**
 * @author Sam
 *
 */
public abstract class AbstractExporter <E, T> {
	
	protected Interceptor<E> _interceptor;
	
	/**
	 * Export {@link Auxhead} component
	 * 
	 * @param columnSize
	 * @param auxhead
	 * @param e
	 */
	protected abstract void exportAuxhead(int columnSize, Auxhead auxhead, E e);
	
	/**
	 * Export header component
	 * 
	 * @param component
	 * @param e
	 */
	protected abstract void exportColumnHeaders(Component component, E e);
	
	/**
	 * Export group component
	 * 
	 * @param columnSize
	 * @param group
	 * @param e
	 */
	protected abstract void exportGroup(int columnSize, Component group, E e);
	
	/**
	 * Export group foot component
	 * 
	 * @param columnSize
	 * @param groupfoot
	 * @param e
	 */
	protected abstract void exportGroupfoot(int columnSize, Component groupfoot, E e);
	
	/**
	 * Export cells component
	 * 
	 * @param rowIndex
	 * @param columnSize
	 * @param row
	 * @param e
	 */
	protected abstract void exportCells(int rowIndex, int columnSize, Component row, E e);
	
	/**
	 * Export footer component
	 * 
	 * @param columnSize
	 * @param target
	 * @param e
	 */
	protected abstract void exportFooters(int columnSize, Component target, E e);
	
	/**
	 * Export tabular component
	 * 
	 * @param component
	 * @param outputStream
	 * @throws Exception
	 */
	protected abstract void exportTabularComponent(MeshElement component, OutputStream outputStream) throws Exception;
	
	/**
	 * Export data 
	 * 
	 * @param columnSize
	 * @param data
	 * @param renderer
	 * @param outputStream
	 * @throws Exception
	 */
	public abstract <D> void export(int columnSize, Collection<D> data, RowRenderer<T, D> renderer, OutputStream outputStream) throws Exception;
	
	/**
	 * Export data with headers
	 * 
	 * Note that exporter will render header using {@link Interceptor#beforeRendering}
	 * Set {@link #setInterceptor(Interceptor)}} if wish to render header differently   
	 *  
	 * 
	 * @param columnHeaders
	 * @param data
	 * @param renderer
	 * @param outputStream
	 * @throws Exception
	 */
	public abstract <D> void export(String[] columnHeaders, final Collection<D> data, RowRenderer<T, D> renderer, OutputStream outputStream) throws Exception;
	
	/**
	 * Export group data
	 * 
	 * @param columnSize
	 * @param data
	 * @param renderer
	 * @param outputStream
	 * @throws Exception
	 */
	public abstract <D> void export(int columnSize, final Collection<Collection <D>> data, GroupRenderer<T, D> renderer, OutputStream outputStream) throws Exception;
	
	/**
	 * Export group data with headers
	 * 
	 * Note that exporter will render header using {@link Interceptor#beforeRendering}
	 * Set {@link #setInterceptor(Interceptor)}} if wish to render header differently   
	 * 
	 * @param columnHeaders
	 * @param data
	 * @param renderer
	 * @param outputStream
	 * @throws Exception
	 */
	public abstract <D> void export(String[] columnHeaders, final Collection<Collection<D>> data, GroupRenderer<T, D> renderer, OutputStream outputStream) throws Exception;
	
	/**
	 * Export header component
	 * 
	 * @param columnSize
	 * @param target
	 * @param e
	 */
	protected void exportHeaders(int columnSize, MeshElement target, E e) {
		List<Component> children = target.getChildren();
		for (Component cmp : children) {
			if (cmp instanceof Auxhead) {
				exportAuxhead(columnSize, (Auxhead)cmp, e);
			} else if (cmp instanceof Columns || cmp instanceof Listhead || cmp instanceof Treecols) {
				exportColumnHeaders(cmp, e);
			}
		}
	}
	
	/**
	 * Export {@link MeshElement}
	 * 
	 * @param columnSize
	 * @param target
	 * @param e
	 */
	protected void exportRows(int columnSize, MeshElement target, E e) {
		int rowIndex = 0;
		List<Component> rows = null;
		if (target instanceof Grid)
			rows = ((Component)invokeComponentGetter(target, "getRows")).getChildren();
		else if (target instanceof Listbox)
			rows = target.getChildren();
		else // tree
			rows = getTreeRows(target);

		for (Component cmp : rows) {
			if (cmp instanceof Listitem || cmp instanceof Row) {
				if (cmp instanceof Listgroup || cmp instanceof Group) {
					exportGroup(columnSize, cmp, e);
				} else if (cmp instanceof Listgroupfoot || cmp instanceof Groupfoot){
					exportGroupfoot(columnSize, cmp, e);
				}
			} else
				exportCells(rowIndex++, columnSize, cmp, e);
		}
	}
	
	private List<Component> getTreeRows(Component root) {
		List<Component> rows = new ArrayList<Component>();
		
		if (root == null)
			return null;
		
		for (Component child : root.getChildren()) {
			if (child instanceof Treerow)
				rows.add(child);
			else
				rows.addAll(getTreeRows(child));
		}
		return rows;
	}
	
	/**
	 * Export {@link MeshElement}
	 * 
	 * @param component
	 * @param outputStream
	 * @throws Exception
	 */
	public void export(MeshElement component, OutputStream outputStream) throws Exception {
		if (component == null)
			throw new RuntimeException("export target reference is null");
		
		exportTabularComponent(component, outputStream);
	}
	
	/**
	 * Sets {@link Interceptor}
	 * 
	 * @param interceptor
	 */
	public void setInterceptor(Interceptor<E> interceptor) {
		_interceptor = interceptor;
	}
	
	/**
	 * Returns {@link Interceptor}
	 * 
	 * @return
	 */
	public Interceptor<E> getInterceptor() {
		return _interceptor;
	}
}
