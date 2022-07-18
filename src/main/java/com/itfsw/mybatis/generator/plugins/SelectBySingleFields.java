package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.utils.*;
import com.itfsw.mybatis.generator.plugins.utils.hook.ISelectOneByExamplePluginHook;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @Author hjw
 * @Date 2022/7/15 16:23
 */
public class SelectBySingleFields extends BasePlugin {

    private XmlElement selectByExampleEle;
    public String METHOD_SELECT_ONE_BY_EXAMPLE = "";



    @Override
    public boolean validate(List<String> warnings) {
        // 插件使用前提是使用了ModelColumnPlugin插件
        if (!PluginTools.checkDependencyPlugin(getContext(), ModelColumnPlugin.class)) {
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件需配合com.itfsw.mybatis.generator.plugins.ModelColumnPlugin插件使用！");
            return false;
        }
        return super.validate(warnings);
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {

        super.initialized(introspectedTable);
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // 方法生成 selectOneByExample
        Method selectOneMethod = JavaElementGeneratorTools.generateMethod(
                METHOD_SELECT_ONE_BY_EXAMPLE,
                JavaVisibility.DEFAULT,
                JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable),
                new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example")
        );
        commentGenerator.addGeneralMethodComment(selectOneMethod, introspectedTable);

        // hook
        if (PluginTools.getHook(ISelectOneByExamplePluginHook.class).clientSelectOneByExampleWithoutBLOBsMethodGenerated(selectOneMethod, interfaze, introspectedTable)) {
            // interface 增加方法
            FormatTools.addMethodWithBestPosition(interfaze, selectOneMethod);
            logger.debug("itfsw(查询单条数据插件):" + interfaze.getType().getShortName() + "增加selectOneByExample方法。");
        }
        return super.clientSelectByExampleWithoutBLOBsMethodGenerated(selectOneMethod, interfaze, introspectedTable);
    }

    /**
     * 生成select xml
     * @author hjw
     * @date 2022/7/15 16:44
     * @param element
     * @param introspectedTable
     * @return boolean
     */
    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        // ------------------------------------ selectOneByExample ----------------------------------
        // 生成查询语句
        XmlElement selectOneElement = new XmlElement("select");
        // 添加注释(!!!必须添加注释，overwrite覆盖生成时，@see XmlFileMergerJaxp.isGeneratedNode会去判断注释中是否存在OLD_ELEMENT_TAGS中的一点，例子：@mbg.generated)
        commentGenerator.addComment(selectOneElement);

        // 添加ID
        selectOneElement.addAttribute(new Attribute("id", METHOD_SELECT_ONE_BY_EXAMPLE));
        // 添加返回类型
        selectOneElement.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
        // 添加参数类型
        selectOneElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
        selectOneElement.addElement(new TextElement("select"));

        StringBuilder sb = new StringBuilder();
        if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
            sb.append('\'');
            sb.append(introspectedTable.getSelectByExampleQueryId());
            sb.append("' as QUERYID,");
            selectOneElement.addElement(new TextElement(sb.toString()));
        }
        selectOneElement.addElement(XmlElementGeneratorTools.getBaseColumnListElement(introspectedTable));

        sb.setLength(0);
        sb.append("from ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        selectOneElement.addElement(new TextElement(sb.toString()));
        selectOneElement.addElement(XmlElementGeneratorTools.getExampleIncludeElement(introspectedTable));

        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "orderByClause != null"));  //$NON-NLS-2$
        ifElement.addElement(new TextElement("order by ${orderByClause}"));
        selectOneElement.addElement(ifElement);

        // 只查询一条
        selectOneElement.addElement(new TextElement("limit 1"));
        this.selectByExampleEle = selectOneElement;
        return super.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }


    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        if (selectByExampleEle != null) {
            // hook
            if (PluginTools.getHook(ISelectOneByExamplePluginHook.class).sqlMapSelectOneByExampleWithoutBLOBsElementGenerated(document, selectByExampleEle, introspectedTable)) {
                // 添加到根节点
                FormatTools.addElementWithBestPosition(document.getRootElement(), selectByExampleEle);
                logger.debug("itfsw(查询单条数据插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加selectOneByExample方法。");
            }
        }

        return true;
    }

}
