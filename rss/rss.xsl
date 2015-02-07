<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/documentcollection">
  <html>
  <body>
  <H2>RSS Aggregator</H2>
    <xsl:for-each select="document/rss/channel">
   <h3>
   <a>
   <xsl:attribute name="href"><xsl:value-of select="link"></xsl:value-of></xsl:attribute>
  <span> <xsl:value-of select="title"></xsl:value-of></span>
   </a>
   </h3>
    <table>
   <xsl:for-each select="item">
  <tr>
  <td>
  <a>
   <xsl:attribute name="href"><xsl:value-of select="link"></xsl:value-of></xsl:attribute>
  <span><xsl:value-of select="title"></xsl:value-of></span> 
   </a>
  
  </td>
  <td>

  <xsl:value-of select="description"></xsl:value-of>
  
  </td>

  </tr>

   </xsl:for-each>
     </table>
   </xsl:for-each>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>



