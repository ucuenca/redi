package org.apache.marmotta.ucuenca.wk.pubman.job;

import org.apache.marmotta.commons.sesame.transactions.api.TransactionListener;
import org.apache.marmotta.commons.sesame.transactions.model.TransactionData;
import org.apache.marmotta.commons.sesame.transactions.sail.KiWiTransactionalSail;
import org.apache.marmotta.kiwi.config.KiWiConfiguration;
import org.apache.marmotta.kiwi.persistence.KiWiDialect;
import org.apache.marmotta.kiwi.persistence.h2.H2Dialect;
import org.apache.marmotta.kiwi.sail.KiWiStore;
import org.apache.marmotta.kiwi.versioning.model.Version;
import org.apache.marmotta.kiwi.versioning.sail.KiWiVersioningSail;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.slf4j.Logger;

public class AuthorVersioningJob {
	
	public Logger log;
	
	public AuthorVersioningJob(Logger log) {
		this.log = log;
	}
	
	public void proveSomething() {
		String defaultContext  = "http://localhost/context/default";
		defaultContext = "http://www.w3.org/ns/ldp#";
		String inferredContext = "http://localhost/context/inferred";
		KiWiDialect dialect    = new H2Dialect();
		KiWiConfiguration conf = new KiWiConfiguration("test", "jdbc:h2:/tmp/Watiqay Khuska/db/marmotta;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=10", 
				"sa", "sa", dialect, defaultContext, inferredContext);
		KiWiStore store = new KiWiStore(conf);
		//= new KiWiStore("test",jdbcUrl,jdbcUser,jdbcPass,dialect, "http://localhost/context/default", "http://localhost/context/inferred");
		KiWiTransactionalSail tsail = new KiWiTransactionalSail(store);
		tsail.addTransactionListener(new TransactionListener() {
			
			@Override
			public void rollback(TransactionData data) {
				// TODO Auto-generated method stub
				log.info("rollback ==> " + data.toString());
				
				
			}
			
			@Override
			public void beforeCommit(TransactionData data) {
				// TODO Auto-generated method stub
				log.info("before commit ==> " + data.toString());
				
			}
			
			@Override
			public void afterCommit(TransactionData data) {
				// TODO Auto-generated method stub
				log.info("after commit ==> " + data.toString());
				
			}
		});
		KiWiVersioningSail vsail = new KiWiVersioningSail(tsail);
		vsail.addTransactionListener(new TransactionListener() {
			
			@Override
			public void rollback(TransactionData data) {
				// TODO Auto-generated method stub
				log.info("rollback2 ==> " + data.toString());
				
			}
			
			@Override
			public void beforeCommit(TransactionData data) {
				// TODO Auto-generated method stub
				log.info("before commit2 ==> " + data.toString());
				
			}
			
			@Override
			public void afterCommit(TransactionData data) {
				// TODO Auto-generated method stub
				log.info("after commit2 ==> " + data.toString());
				
			}
		});
		this.testRepository(vsail);
		
		
	}
	
	private void testRepository(KiWiVersioningSail vsail) {
		SailRepository repository = new SailRepository(vsail);
		try {
			repository.initialize();
			
			RepositoryConnection con = repository.getConnection();
		    try {
		    	
		    	ValueFactory factory = repository.getValueFactory();
		        con.begin();
		        
		        URI bob = factory.createURI("http://example.org/bob");
		        //URI name = factory.createURI("http://example.org/name");
		        Literal bobsName = factory.createLiteral("Bob");
		        Statement nameStatement = factory.
		        		createStatement(bob,  org.openrdf.model.vocabulary.FOAF.NAME, bobsName);
		     // create a new Model to put statements in
		        Model model = new LinkedHashModel();
		        model.add(nameStatement);
		        con.add(nameStatement);
		        con.commit();
		    } finally {
		        con.close();
		    }
	
			// do something with the repository (e.g. add data)
			//...
	
			// list all versions (note that there are many methods with different parameters, including a subject resource,
			// a date range, or both)
			RepositoryResult<Version> versions = vsail.listVersions();
			try {
			    while(versions.hasNext()) {
			        Version v = versions.next();
			        v.getClass();
	
			        // do something with v
			    }
			} finally {
			    versions.close();
			}
	
			// get a snapshot connection for a certain date
			//Date snapshotDate = new Date(2014,06,08);
			/*RepositoryConnection snapshotConnection = vsail.getSnapshot(snapshotDate);
			try {
			    // access the triples in the snapshot as they were at the time of the snapshot
			    //...
			} finally {
			    snapshotConnection.close();
			}*/
		}catch(Exception e) {
			e.getMessage();
			//e.printStackTrace();
		}
		
	}

}
