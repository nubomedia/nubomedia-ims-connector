//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.09.18 at 09:29:28 PM MESZ 
//


package de.fhg.fokus.ims.core.event.reg;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kxml2.kdom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import de.fhg.fokus.ims.core.utils.xml.XmlSerializable;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:ietf:params:xml:ns:reginfo}contact" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="aor" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="state" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="init"/>
 *             &lt;enumeration value="active"/>
 *             &lt;enumeration value="terminated"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class Registration implements XmlSerializable
{
	public static final class State extends de.fhg.fokus.ims.core.utils.Enum
	{
		public static final State INIT = new State("init");
		public static final State ACTIVE = new State("active");
		public static final State TERMINATED = new State("terminated");
		
		private State(String value)
		{
			super(value);
		}
	}
	
    private List contact;
    
    private List any;
    
    private String aor;
    
    private String id;
    
    private State state;

    /**
     * Gets the value of the contact property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contact property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContact().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Contact }
     * 
     * 
     */
    public List getContact() {
        if (contact == null) {
            contact = new ArrayList();
        }
        return this.contact;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link Object }
     * 
     * 
     */
    public List getAny() {
        if (any == null) {
            any = new ArrayList();
        }
        return this.any;
    }

    /**
     * Gets the value of the aor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAor() {
        return aor;
    }

    /**
     * Sets the value of the aor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAor(String value) {
        this.aor = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setState(State value) {
        this.state = value;
    }

	public void deserialize(XmlPullParser reader) throws XmlPullParserException, IOException
	{
		aor = reader.getAttributeValue(XmlPullParser.NO_NAMESPACE, "aor");		
		id = reader.getAttributeValue(XmlPullParser.NO_NAMESPACE, "id");
		state = (State) de.fhg.fokus.ims.core.utils.Enum.parse(State.class, reader.getAttributeValue(XmlPullParser.NO_NAMESPACE, "state"));
		
		String startTagName = reader.getName();

		int eventType = reader.nextTag();
		String namespace = reader.getNamespace();
		String name = reader.getName();

		while (!(eventType == XmlPullParser.END_TAG & name.equals(startTagName)))
		{
			if (Reginfo.NAMESPACE.equals(namespace))
			{
				if (name.equals("contact"))
				{
					Contact contact = new Contact();
					contact.deserialize(reader);
					this.getContact().add(contact);
				}
			} 
			else
			{
				Element element = new Element();
				element.parse(reader);
				getAny().add(element);
			}
			
			eventType = reader.nextTag();
			namespace = reader.getNamespace();
			name = reader.getName();
		}

	}

	public void serialize(XmlSerializer writer) throws IOException
	{
		writer.attribute(XmlPullParser.NO_NAMESPACE, "aor", aor);
		
		writer.attribute(XmlPullParser.NO_NAMESPACE, "id", id);
		
		writer.attribute(XmlPullParser.NO_NAMESPACE, "state", state.toString());
		
		for(int i = 0; i < contact.size();i++)
		{
			writer.startTag(Reginfo.NAMESPACE, "contact");
			((Contact)contact.get(i)).serialize(writer);
			writer.endTag(Reginfo.NAMESPACE, "contact");
		}
	}

}
