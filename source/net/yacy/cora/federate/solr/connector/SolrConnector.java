/**
 *  SolrConnector
 *  Copyright 2011 by Michael Peter Christen
 *  First released 13.09.2011 at http://yacy.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package net.yacy.cora.federate.solr.connector;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import net.yacy.cora.sorting.ReversibleScoreMap;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

public interface SolrConnector extends Iterable<String> /* Iterable of document IDs */ {

    /**
     * get the solr autocommit delay
     * @return the maximum waiting time after a solr command until it is transported to the server
     */
    public int getCommitWithinMs();

    /**
     * set the solr autocommit delay
     * @param c the maximum waiting time after a solr command until it is transported to the server
     */
    public void setCommitWithinMs(int c);

    /**
     * force a commit
     */
    public void commit(boolean softCommit);

    /**
     * force an explicit merge of segments
     * @param maxSegments the maximum number of segments. Set to 1 for maximum optimization
     */
    public void optimize(int maxSegments);
    
    /**
     * close the server connection
     */
    public void close();

    /**
     * delete everything in the solr index
     * @throws IOException
     */
    public void clear() throws IOException;

    /**
     * delete an entry from solr
     * @param id the url hash of the entry
     * @throws IOException
     */
    public void delete(final String id) throws IOException;

    /**
     * delete a set of entries from solr; entries are identified by their url hash
     * @param ids a list of url hashes
     * @throws IOException
     */
    public void delete(final List<String> ids) throws IOException;

    /**
     * delete entries from solr according the given solr query string
     * @param id the url hash of the entry
     * @return the number of deletions
     * @throws IOException
     */
    public int deleteByQuery(final String querystring) throws IOException;

    /**
     * check if a given key exists in solr at the field fieldName
     * @param fieldName
     * @param key
     * @return true if any entry in solr exists
     * @throws IOException
     */
    public boolean exists(final String fieldName, final String key) throws IOException;

    /**
     * add a solr input document
     * @param solrdoc
     * @throws IOException
     * @throws SolrException
     */
    public void add(final SolrInputDocument solrdoc) throws IOException, SolrException;
    /**
     * get a field value from solr by given key for the id-field and a field name
     * @param key
     * @param field one field
     * @return one result or null if no result exists
     * @throws IOException
     */
    public Object getFieldById(final String key, final String field) throws IOException;
    
    /**
     * get a document from solr by given key for the id-field
     * @param key
     * @param fields list of fields
     * @return one result or null if no result exists
     * @throws IOException
     */
    public SolrDocument getById(final String key, final String ... fields) throws IOException;

    /**
     * get a query result from solr
     * @param query
     * @throws IOException
     */
    public QueryResponse query(final ModifiableSolrParams query) throws IOException, SolrException;

    /**
     * get a query result from solr
     * to get all results set the query String to "*:*"
     * @param querystring the solr query string
     * @param offset the first result offset
     * @param count number of wanted results
     * @param fields list of fields
     * @throws IOException
     */
    public SolrDocumentList query(final String querystring, final int offset, final int count, final String ... fields) throws IOException, SolrException;

    /**
     * get the number of results when this query is done.
     * This should only be called if the actual result is never used, and only the count is interesting
     * @param querystring
     * @return the number of results for this query
     */
    public long getQueryCount(final String querystring) throws IOException;

    /**
     * get facets of the index: a list of lists with values that are most common in a specific field
     * @param query a query which is performed to get the facets
     * @param maxresults the maximum size of the resulting maps
     * @param fields the field names which are selected as facet
     * @return a map with key = facet field name, value = an ordered map of field values for that field
     * @throws IOException
     */
    public Map<String, ReversibleScoreMap<String>> getFacets(String query, int maxresults, final String ... fields) throws IOException;
    
    /**
     * Get a query result from solr as a stream of documents.
     * The result queue is considered as terminated if AbstractSolrConnector.POISON_DOCUMENT is returned.
     * The method returns immediately and feeds the search results into the queue
     * @param querystring the solr query string
     * @param offset first result offset
     * @param maxcount the maximum number of results
     * @param maxtime the maximum time in milliseconds
     * @param buffersize the size of an ArrayBlockingQueue; if <= 0 then a LinkedBlockingQueue is used
     * @param fields list of fields
     * @return a blocking queue which is terminated  with AbstractSolrConnector.POISON_DOCUMENT as last element
     */
    public BlockingQueue<SolrDocument> concurrentQuery(final String querystring, final int offset, final int maxcount, final long maxtime, final int buffersize, final String ... fields);

    /**
     * get a document id result stream from a solr query.
     * The result queue is considered as terminated if AbstractSolrConnectro.POISON_ID is returned.
     * The method returns immediately and feeds the search results into the queue
     * @param querystring
     * @param offset
     * @param maxcount
     * @return
     */
    public BlockingQueue<String> concurrentIDs(final String querystring, final int offset, final int maxcount, final long maxtime);

    /**
     * get the size of the index
     * @return number of results if solr is queries with a catch-all pattern
     */
    public long getSize();

}
