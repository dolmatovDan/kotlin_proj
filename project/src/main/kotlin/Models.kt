import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "ValCurs")
data class ValCurs(
    @field:JacksonXmlProperty(localName = "Date", isAttribute = true)
    val date: String,

    @field:JacksonXmlProperty(localName = "name", isAttribute = true)
    val name: String,

    @field:JacksonXmlElementWrapper(useWrapping = false)
    @field:JacksonXmlProperty(localName = "Valute")
    val valutes: List<Valute> = emptyList()
)

data class Valute(
    @field:JacksonXmlProperty(localName = "ID", isAttribute = true)
    val id: String,

    @field:JacksonXmlProperty(localName = "NumCode")
    val numCode: String,

    @field:JacksonXmlProperty(localName = "CharCode")
    val charCode: String,

    @field:JacksonXmlProperty(localName = "Nominal")
    val nominal: Int,

    @field:JacksonXmlProperty(localName = "Name")
    val name: String,

    @field:JacksonXmlProperty(localName = "Value")
    val value: String,

    @field:JacksonXmlProperty(localName = "VunitRate")
    val vunitRate: String
)