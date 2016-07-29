package gov.nist.javax.sip.parser;

import gov.nist.core.NameValue;
import gov.nist.javax.sip.header.AcceptContact;
import gov.nist.javax.sip.header.SIPHeader;

import java.text.ParseException;

public class AcceptContactParser extends ParametersParser
{
	protected AcceptContactParser(Lexer lexer)
	{
		super(lexer);
	}

	public AcceptContactParser(String buffer)
	{
		super(buffer);
	}

	public SIPHeader parse() throws ParseException
	{
		// past the header name and the colon.
		headerName(TokenTypes.ACCEPT_CONTACT);

		AcceptContact retval = new AcceptContact();
		this.lexer.SPorHT();

		if (lexer.lookAhead(0) == '*')
			lexer.consume(1);
		if (lexer.lookAhead(0) == ';')
			lexer.consume(1);

		retval.removeParameters();
		if (lexer.startsId())
		{
			while (true)
			{
				this.lexer.SPorHT();
				NameValue nv = nameValue();
				retval.setParameter(nv.getName(), (nv.getValue() != null && nv.getValue().equals("") ? (String)null : (String)nv.getValue()));
				// eat white space
				this.lexer.SPorHT();
				if (lexer.lookAhead(0) != ';')
					break;
				else
					lexer.consume(1);
			}
		}
		return retval;
	}

}
