package org.cytoscape.vsdl3c.internal.task;

import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.cytoscape.vsdl3c.internal.Util;
import org.cytoscape.vsdl3c.internal.model.SPARQLEndpoint;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Query the endpoint for its graph URI list
 *
 */
public class QueryGraphTask extends AbstractTask {

	private List<String> list;
	private SPARQLEndpoint endpoint;

	public QueryGraphTask(SPARQLEndpoint endpoint, List<String> list) {
		this.list = list;
		this.endpoint = endpoint;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		Util.setAuth(endpoint);
		taskMonitor.setTitle("Query graphs for: " + endpoint.getUri());
		String query = "SELECT DISTINCT ?g WHERE {  GRAPH ?g { ?s ?p ?o } }";

		QueryEngineHTTP qexec = (QueryEngineHTTP) QueryExecutionFactory
				.sparqlService(endpoint.getUri(), query);
		qexec = Util.setAPIKey(qexec, endpoint);

		try {
			ResultSet r = qexec.execSelect();

			while (r.hasNext()) {
				QuerySolution s = r.nextSolution();
				RDFNode node = s.get("g");
				list.add(node.toString());
			}

		} catch (Exception ex) {
			if (ex instanceof QueryException) {
				JOptionPane.showMessageDialog(null, ex.getMessage() + ": "
						+ endpoint.getUri());
				System.out.println("Error at: " + endpoint.getUri());
			}
			ex.printStackTrace();
		}

		if (list.size() == 0) {
			list.add("--- No SPARQL Endpoint ---");
		}
		Collections.sort(list);
	}
}
