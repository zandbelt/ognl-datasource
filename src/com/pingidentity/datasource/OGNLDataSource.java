/**
 * Copyright (C) 2012 Ping Identity Corporation
 * All rights reserved.
 *
 * The contents of this file are the property of Ping Identity Corporation.
 * For further information please contact:
 *
 *      Ping Identity Corporation
 *      1099 18th St Suite 2950
 *      Denver, CO 80202
 *      303.468.2900
 *      http://www.pingidentity.com
 *
 * DISCLAIMER OF WARRANTIES:
 *
 * THE SOFTWARE PROVIDED HEREUNDER IS PROVIDED ON AN "AS IS" BASIS, WITHOUT
 * ANY WARRANTIES OR REPRESENTATIONS EXPRESS, IMPLIED OR STATUTORY; INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF QUALITY, PERFORMANCE, NONINFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  NOR ARE THERE ANY
 * WARRANTIES CREATED BY A COURSE OR DEALING, COURSE OF PERFORMANCE OR TRADE
 * USAGE.  FURTHERMORE, THERE ARE NO WARRANTIES THAT THE SOFTWARE WILL MEET
 * YOUR NEEDS OR BE FREE FROM ERRORS, OR THAT THE OPERATION OF THE SOFTWARE
 * WILL BE UNINTERRUPTED.  IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * 
 * This class implements a custom data source fore PingFederate.
 * This data source does not go out to any actual backend data store to retrieve
 * user attributes, but rather processes existing attributes with a configured
 * OGNL expression.
 * 
 * As such this datasource can be used to apply an OGNL expression to
 * transform existing attributes in to a value that can be used as input
 * to filters for other datasources that are subsequently be added to
 * a connection.
 * 
 * The attribute name that results from the OGNL expression evaluation is
 * currently fixed to "result". The OGNL expression itself is passed in
 * through the "filter" settings of the data source, so you can refer to
 * existing attributes set earlier in the process by using the ${attribute}
 * syntax.
 * 
 * Example OGNL expression that can be configured in the filter:
 * "${email}".substring(0, "${email}".indexOf("@"))
 * 
 * (for stripping the subdomain from the e-mail address and put it in the result)
 *
 * Author: Hans Zandbelt - hzandbelt@pingidentity.com
 * 
 **************************************************************************/

package com.pingidentity.datasource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import ognl.Ognl;

import org.sourceid.saml20.adapter.conf.Configuration;
import org.sourceid.saml20.adapter.conf.SimpleFieldList;
import org.sourceid.saml20.adapter.gui.AdapterConfigurationGuiDescriptor;
import org.sourceid.saml20.adapter.gui.TextFieldDescriptor;

//import sun.rmi.runtime.Log;

import com.pingidentity.common.util.ognl.ExpressionCalculator;
import com.pingidentity.sources.CustomDataSourceDriver;
import com.pingidentity.sources.CustomDataSourceDriverDescriptor;
import com.pingidentity.sources.SourceDescriptor;
import com.pingidentity.sources.gui.FilterFieldsGuiDescriptor;

import org.sourceid.saml20.adapter.conf.Field;

public class OGNLDataSource implements CustomDataSourceDriver {

	String attributeName = "result";
	CustomDataSourceDriverDescriptor descriptor;

	public List<String> getAvailableFields() {
		List<String> theList = new Vector<String>();
		theList.add(attributeName);
		return theList;
	}

	public Map<String, Object> retrieveValues(Collection<String> list,
			SimpleFieldList filterlist) {

		Iterator<Field> iter = filterlist.getFields().iterator();
		while (iter.hasNext()) {
			System.err.println(iter.next().toString());
		}

		String expr = filterlist.getFieldValue("expression");

		Map<String, Object> theMap = new HashMap<String,Object>();

		try {
			Object parsedExpression = Ognl.parseExpression(expr);
			Object value = ExpressionCalculator.calculate(parsedExpression, null, null);
			theMap.put(attributeName, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return theMap;
	}

	public boolean testConnection() {
		return true;
	}

	public void configure(Configuration config) {
		//attributeName = config.getFieldValue("attribute");
	}

	public SourceDescriptor getSourceDescriptor() {
		AdapterConfigurationGuiDescriptor desc = new AdapterConfigurationGuiDescriptor();
		desc.setDescription("OGNL Datasource");

		//TextFieldDescriptor attribute = new TextFieldDescriptor("attribute",
		//		"Attribute Name");
		//desc.addField(attribute);

		TextFieldDescriptor expression = new TextFieldDescriptor("expression",
				"OGNL expression");

		FilterFieldsGuiDescriptor filter = new FilterFieldsGuiDescriptor();
		filter.addField(expression);

		descriptor = new CustomDataSourceDriverDescriptor(this,
				"OGNL Data Source", desc, filter);

		return descriptor;
	}

}
