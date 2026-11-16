<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" encoding="UTF-8" indent="no"/>

    <xsl:param name="role"/>
    <xsl:param name="base"/>
    <xsl:param name="current"/>

    <xsl:template match="/menu">
        <nav class="menu" aria-label="Главное меню">
            <xsl:apply-templates select="group[item[contains(concat(' ', @roles, ' '), concat(' ', $role, ' '))]]"/>
        </nav>
    </xsl:template>

    <xsl:template match="group">
        <div class="menu-group">
            <div class="menu-title"><xsl:value-of select="@title"/></div>
            <ul>
                <xsl:apply-templates select="item[contains(concat(' ', @roles, ' '), concat(' ', $role, ' '))]"/>
            </ul>
        </div>
    </xsl:template>

    <xsl:template match="item">
        <li>
            <a href="{$base}{@href}">
                <xsl:if test="@href = $current">
                    <xsl:attribute name="class">active</xsl:attribute>
                    <xsl:attribute name="aria-current">page</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="@title"/>
            </a>
        </li>
    </xsl:template>
</xsl:stylesheet>
