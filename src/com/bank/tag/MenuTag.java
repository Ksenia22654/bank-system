package com.bank.tag;

import com.bank.model.User;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/** Преобразует XML-описание меню в HTML с помощью XSLT с учётом роли пользователя. */
public class MenuTag extends SimpleTagSupport {
    private static volatile Templates templates;

    @Override
    public void doTag() throws JspException, IOException {
        PageContext page = (PageContext) getJspContext();
        HttpServletRequest request = (HttpServletRequest) page.getRequest();
        ServletContext context = page.getServletContext();
        User user = (User) request.getAttribute("currentUser");
        String currentPath = (String) request.getAttribute("javax.servlet.forward.path_info");
        try (InputStream xml = context.getResourceAsStream("/WEB-INF/menu/menu.xml")) {
            Transformer transformer = templates(context).newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setParameter("role", user == null ? "" : user.getRoleCode());
            transformer.setParameter("base", request.getContextPath());
            transformer.setParameter("current", currentPath == null ? "" : "/app" + currentPath);
            StringWriter html = new StringWriter();
            transformer.transform(new StreamSource(xml), new StreamResult(html));
            page.getOut().write(html.toString());
        } catch (TransformerException e) {
            throw new JspException("Не удалось сформировать меню", e);
        }
    }

    private static Templates templates(ServletContext context) throws TransformerException, IOException {
        if (templates == null) {
            synchronized (MenuTag.class) {
                if (templates == null) {
                    TransformerFactory factory = TransformerFactory.newInstance();
                    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    try (InputStream xsl = context.getResourceAsStream("/WEB-INF/menu/menu.xsl")) {
                        templates = factory.newTemplates(new StreamSource(xsl));
                    }
                }
            }
        }
        return templates;
    }
}
