package com.yinheli.mybatis.plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.JavaElement;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * @author yinheli <yinheli@gmail.com>
 * @date 2012-7-28 上午9:50:49
 * @version V1.0
 */
public class CommentPlugin extends PluginAdapter {

	/**
	 * validate 
	 * @see org.mybatis.generator.api.Plugin#validate(java.util.List)   
	 */
	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}
	
	/**
	 * modelBaseRecordClassGenerated 
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
			IntrospectedTable introspectedTable) {
		topLevelClass.getJavaDocLines().clear();
		topLevelClass.addJavaDocLine("/**");
		topLevelClass.addJavaDocLine(" * Table: " + introspectedTable.getFullyQualifiedTable());
		topLevelClass.addJavaDocLine(" */");
		return true;
	}
	
	/**
	 * modelFieldGenerated 
	 */
	@Override
	public boolean modelFieldGenerated(Field field,
			TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
			IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		comment(field, introspectedTable, introspectedColumn);
		return true;
	}
	
	/**
	 * modelGetterMethodGenerated 
	 */
	@Override
	public boolean modelGetterMethodGenerated(Method method,
			TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
			IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		comment(method, introspectedTable, introspectedColumn);
		return true;
	}
	
	/**
	 * modelSetterMethodGenerated 
	 */
	@Override
	public boolean modelSetterMethodGenerated(Method method,
			TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
			IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		comment(method, introspectedTable, introspectedColumn);
		return true;
	}
	
	/**
	 * 添加注释
	 * 
	 * @param element
	 * @param introspectedTable
	 * @param introspectedColumn
	 * @author yinheli
	 * @date 2012-7-28 上午10:44:25
	 */
	private void comment(JavaElement element, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
		element.getJavaDocLines().clear();
		element.addJavaDocLine("/**");
		String remark = introspectedColumn.getRemarks();
		if (remark != null && remark.length() > 1) {
			element.addJavaDocLine(" * " + remark);
			element.addJavaDocLine(" *");
		}
		element.addJavaDocLine(" * Table:     " + introspectedTable.getFullyQualifiedTable());
		element.addJavaDocLine(" * Column:    " + introspectedColumn.getActualColumnName());
		element.addJavaDocLine(" * Nullable:  " + introspectedColumn.isNullable());
		element.addJavaDocLine(" */");
	}
	
	/**
	 * sqlMapResultMapWithoutBLOBsElementGenerated 
	 */
	@Override
	public boolean sqlMapResultMapWithoutBLOBsElementGenerated(
			XmlElement element, IntrospectedTable introspectedTable) {
		commentResultMap(element, introspectedTable);
		return true;
	}
	
	/**
	 * sqlMapResultMapWithBLOBsElementGenerated 
	 */
	@Override
	public boolean sqlMapResultMapWithBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		commentResultMap(element, introspectedTable);
		return true;
	}
	
	/**
	 * 为resultMap添加注释
	 * 
	 * @param element
	 * @param introspectedTable
	 * @author yinheli
	 * @date 2012-7-28 下午12:17:00
	 */
	void commentResultMap(XmlElement element,
			IntrospectedTable introspectedTable) {
		List<Element> es = element.getElements();
		if (es.isEmpty()) {
			return;
		}
		
		String alias = introspectedTable.getTableConfiguration().getAlias();
		int aliasLen = -1;
		if (alias != null) {
			aliasLen = alias.length() + 1;
		}
		
		Iterator<Element> it = es.iterator();
		
		Map<Element, Element> map = new HashMap<Element, Element>();
		
		while (it.hasNext()) {
			Element e = it.next();

			if (e instanceof TextElement) {
				it.remove();
				continue;
			} 
			
			XmlElement el = (XmlElement) e;
			List<Attribute> as = el.getAttributes();
			if (as.isEmpty()) {
				continue;
			}
			
			String col = null;
			for (Attribute a : as) {
				if (a.getName().equalsIgnoreCase("column")) {
					col = a.getValue();
					break;
				}
			}
			
			if (col == null) {
				continue;
			}
			
			if (aliasLen > 0) {
				col = col.substring(aliasLen);
			}
			
			IntrospectedColumn ic = introspectedTable.getColumn(col);
			if (ic == null) {
				continue;
			}
			
			StringBuilder sb = new StringBuilder();
			if (ic.getRemarks() != null && ic.getRemarks().length() > 1) {
				sb.append("<!-- ");
				sb.append(ic.getRemarks());
				sb.append(" -->");
				map.put(el, new TextElement(sb.toString()));
			}
		}
		
		if (map.isEmpty()) {
			return;
		}
		
		Set<Element> set = map.keySet();
		for (Element e : set) {
			int id = es.indexOf(e);
			es.add(id, map.get(e));
			es.add(id, new TextElement(""));
		}
		
	}
	
	/**
	 * sqlMapInsertElementGenerated 
	 */
	@Override
	public boolean sqlMapInsertElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		removeAttribute(element.getAttributes(), "parameterType");	
		return true;
	}
	
	/**
	 * sqlMapInsertSelectiveElementGenerated 
	 */
	@Override
	public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		removeAttribute(element.getAttributes(), "parameterType");	
		return true;
	}
	
	/**
	 * sqlMapUpdateByPrimaryKeySelectiveElementGenerated 
	 */
	@Override
	public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(
			XmlElement element, IntrospectedTable introspectedTable) {
		removeAttribute(element.getAttributes(), "parameterType");	
		return true;
	}
	
	/**
	 * sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated 
	 */
	@Override
	public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(
			XmlElement element, IntrospectedTable introspectedTable) {
		removeAttribute(element.getAttributes(), "parameterType");	
		return true;
	}
	
	/**
	 * sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated 
	 */
	@Override
	public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(
			XmlElement element, IntrospectedTable introspectedTable) {
		removeAttribute(element.getAttributes(), "parameterType");	
		return true;
	}
	
	/**
	 * 移除 xml attribute
	 * 
	 * @param as
	 * @param name
	 * @author yinheli
	 * @date 2012-7-28 下午12:40:54
	 */
	private void removeAttribute(List<Attribute> as, String name) {
		if (as.isEmpty()) return;
		Iterator<Attribute> it = as.iterator();
		while (it.hasNext()) {
			Attribute attr = it.next();
			if (attr.getName().equalsIgnoreCase(name)) {
				it.remove();
				return;
			}
		}
	}
	
	/**
	 * sqlMapDocumentGenerated 
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document,
			IntrospectedTable introspectedTable) {
		StringBuilder sb = new StringBuilder();
		sb.append("<!-- ### 以上代码由MBG + CommentPlugin自动生成, 生成时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		sb.append(" ### -->\n\n\n");
		document.getRootElement().addElement(new TextElement(""));
		document.getRootElement().addElement(new TextElement(sb.toString()));
		document.getRootElement().addElement(new TextElement("<!-- Your codes goes here!!! -->"));
		document.getRootElement().addElement(new TextElement(""));
		return true;
	}
	
}
