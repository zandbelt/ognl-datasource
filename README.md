# ognl-datasource

A custom data store for PingFederate that parses OGNL expressions

The data source does not go out to any actual backend data store to retrieve
user attributes, but rather processes existing attributes with a configured
OGNL expression.

As such this data store can be used to apply an OGNL expression to
transform existing attributes in to a value that can be used as input
to filters for other data store that are subsequently be added to
a connection.

The attribute name that results from the OGNL expression evaluation is
currently fixed to "result". The OGNL expression itself is passed in
through the "filter" settings of the data source, so you can refer to
existing attributes set earlier in the process by using the ${attribute}
syntax.

Example OGNL expression attribute filter for stripping the subdomain from
the e-mail address and put it in the result:

    "${email}".substring(0, "${email}".indexOf("@"))

Note you really need the double quotes around attribute names because of the 
way the expression is fed as an attribute filter to the OGNL parser.