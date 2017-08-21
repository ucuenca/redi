package at.newmedialab.lmf.worker.model;

/*
 * This implementation has been copied from
 * http://stackoverflow.com/questions/10841760/linkedblockingqueue-with-fast-containsobject-o-method
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */


import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Provides a single-lock queuing algorithm with fast contains(Object o) and
 * remove(Object o), at the expense of higher synchronization cost when
 * compared to {@link LinkedBlockingQueue}.  This queue implementation does not
 * allow for duplicate entries.
 *
 * <p>Use of this particular {@link BlockingQueue} implementation is encouraged
 * when the cost of calling
 * <code>{@link BlockingQueue#contains(Object o)}</code> or
 * <code>{@link BlockingQueue#remove(Object o)}</code> outweighs the throughput
 * benefit if using a {@link LinkedBlockingQueue}.  This queue performs best
 * when few threads require simultaneous access to it.
 *
 * <p>The basic operations this queue provides and their associated run times
 * are as follows, where <i>n</i> is the number of elements in this queue and
 * <i>m</i> is the number of elements in the specified collection, if any such
 * collection is specified:
 *
 * <ul>
 * <li><b>add(E element)</b> - <i>O(1)</i></li>
 * <li><b>addAll(Collection<? extends E> c)</b> - <i>O(m)</i></li>
 * <li><b>drainTo(Collection<? extends E> c, int maxElements)</b>
 *  - <i>O(maxElements*O(</i><code>c.add(Object o)</code><i>))</i></li>
 * <li><b>contains(E element)</b> - <i>O(1)</i></li>
 * <li><b>offer(E element)</b> - <i>O(1)</i></li>
 * <li><b>poll()</b> - <i>O(1)</i></li>
 * <li><b>remove(E element)</b> - <i>O(1)</i></li>
 * <li><b>removeAll(Collection<? extends E> c)</b> - <i>O(m)</i></li>
 * <li><b>retainAll(Collection<? extends E> c)</b> - <i>O(n*O(
 *  </i><code>c.contains(Object o)</code><i>))</i></li>
 * </ul>
 *
 * @param <E> type of element this queue will handle.  It is strongly
 * recommended that the underlying element overrides <code>hashCode()</code>
 * and <code>equals(Object o)</code> in an efficient manner.
 *
 * @author Ben Lawry
 */
public class HashedLinkedBlockingQueue<E> implements BlockingQueue<E>{
    /** Polling removes the head, offering adds to the tail. */
    private Node head, tail;
    /** Required for constant-time lookups and removals. */
    private HashMap<E,Node> contents;
    /** Allows the user to artificially limit the capacity of this queue. */
    private final int maxCapacity;

    //Constructors: -----------------------------------------------------------

    /**
     * Creates an empty queue with max capacity equal to
     * {@link Integer#MAX_VALUE}.
     */
    public HashedLinkedBlockingQueue(){ this(null,Integer.MAX_VALUE); }

    /**
     * Creates an empty queue with max capacity equals to the specified value.
     * @param capacity (1 to {@link Integer#MAX_VALUE})
     */
    public HashedLinkedBlockingQueue(int capacity){
        this(null,Math.max(1,capacity));
    }

    /**
     * Creates a new queue and initializes it to the contents of the specified
     * collection, queued in the order returned by its iterator, with a max
     * capacity of {@link Integer#MAX_VALUE}.
     * @param c collection of elements to add
     */
    public HashedLinkedBlockingQueue(Collection<? extends E> c){
        this(c,Integer.MAX_VALUE);
    }

    /**
     * Creates a new queue and initializes it to the contents of the specified
     * collection, queued in the order returned by its iterator, with a max
     * capacity equal to the specified value.
     * @param c collection of elements to add
     * @param capacity (1 to {@link Integer#MAX_VALUE})
     */
    public HashedLinkedBlockingQueue(Collection<? extends E> c, int capacity){
        maxCapacity = capacity;
        contents = new HashMap<E,Node>();

        if(c == null || c.isEmpty()){
            head = null;
            tail = null;
        }
        else for(E e : c) enqueue(e);
    }

    //Private helper methods: -------------------------------------------------

    private E dequeue(){
        if(contents.isEmpty()) return null;

        Node n = head;
        contents.remove(n.element);

        if(contents.isEmpty()){
            head = null;
            tail = null;
        }
        else{
            head.next.prev = null;
            head = head.next;
            n.next = null;
        }

        return n.element;
    }

    private void enqueue(E e){
        if(contents.containsKey(e)) return;

        Node n = new Node(e);
        if(contents.isEmpty()){
            head = n;
            tail = n;
        }
        else{
            tail.next = n;
            n.prev = tail;
            tail = n;
        }

        contents.put(e,n);
    }

    private void removeNode(Node n, boolean notify){
        if(n == null) return;
        if(n == head) dequeue();
        else if(n == tail){
            tail.prev.next = null;
            tail = tail.prev;
            n.prev = null;
        }
        else{
            n.prev.next = n.next;
            n.next.prev = n.prev;
            n.prev = null;
            n.next = null;
        }

        contents.remove(n.element);
        if(notify) synchronized(contents){ contents.notifyAll(); }
    }

    //Public instance methods: ------------------------------------------------

    public void print(){
        Node n = head;
        int i = 1;
        while(n != null){
            System.out.println(i+": "+n);
            n = n.next;
            i++;
        }
    }

    //Overridden methods: -----------------------------------------------------

    @Override
    public boolean add(E e){
        synchronized(contents){
            if(remainingCapacity() < 1) throw new IllegalStateException();
            enqueue(e);
            contents.notifyAll();
        }

        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c){
        boolean changed = true;
        synchronized(contents){
            for(E e : c){
                if(remainingCapacity() < 1) throw new IllegalStateException();
                enqueue(e);
            }

            contents.notifyAll();
        }
        return changed;
    }

    @Override
    public void clear(){
        synchronized(contents){
            if(isEmpty()) return;
            head = null;
            tail = null;
            contents.clear();
            contents.notifyAll();
        }
    }

    @Override
    public boolean contains(Object o){
        synchronized(contents){ return contents.containsKey(o); }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        synchronized(contents){
            for(Object o : c) if(!contents.containsKey(o)) return false;
        }

        return true;
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return drainTo(c,maxCapacity);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        if(this == c) throw new IllegalArgumentException();
        int transferred = 0;

        synchronized(contents){
            while(!isEmpty() && transferred < maxElements)
                if(c.add(dequeue())) transferred++;
            if(transferred > 0) contents.notifyAll();
        }

        return transferred;
    }

    @Override
    public E element(){
        E e = peek();
        if(e == null) throw new IllegalStateException();
        return e;
    }

    @Override
    public boolean isEmpty() {
        synchronized(contents){ return contents.isEmpty(); }
    }

    @Override
    public Iterator<E> iterator(){ return new Itr(); }

    @Override
    public boolean offer(E e){
        synchronized(contents){
            if(contents.containsKey(e)) return false;
            enqueue(e);
            contents.notifyAll();
        }

        return true;
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit)
            throws InterruptedException{
        long remainingSleep = -1;
        long millis = unit.toMillis(timeout);
        long methodCalled = System.currentTimeMillis();

        synchronized(contents){
            while((remainingSleep =
                    (methodCalled+millis)-System.currentTimeMillis()) > 0 &&
                    (remainingCapacity() < 1 || contents.containsKey(e))){
                contents.wait(remainingSleep);
            }

            if(remainingSleep < 1) return false;
            enqueue(e);
            contents.notifyAll();
        }

        return true;
    }

    @Override
    public E peek(){
        synchronized(contents){ return (head != null) ? head.element : null; }
    }

    @Override
    public E poll(){
        synchronized(contents){
            E e = dequeue();
            if(e != null) contents.notifyAll();
            return e;
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException{
        E e = null;
        long remainingSleep = -1;
        long millis = unit.toMillis(timeout);
        long methodCalled = System.currentTimeMillis();

        synchronized(contents){
            e = dequeue();

            while(e == null && (remainingSleep = (methodCalled+millis)-
                    System.currentTimeMillis()) > 0){
                contents.wait(remainingSleep);
                e = dequeue();
            }

            if(e == null) e = dequeue();
            if(e != null) contents.notifyAll();
        }

        return e;
    }

    @Override
    public void put(E e) throws InterruptedException{
        synchronized(contents){
            while(remainingCapacity() < 1) contents.wait();
            enqueue(e);
            contents.notifyAll();
        }
    }

    @Override
    public int remainingCapacity(){ return maxCapacity-size(); }

    @Override
    public E remove(){
        E e = poll();
        if(e == null) throw new IllegalStateException();
        return e;
    }

    @Override
    public boolean remove(Object o){
        synchronized(contents){
            Node n = contents.get(o);
            if(n == null) return false;
            removeNode(n,true);
        }

        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c){
        if(this == c){
            synchronized(contents){
                if(isEmpty()){
                    clear();
                    return true;
                }
            }

            return false;
        }

        boolean changed = false;

        synchronized(contents){
            for(Object o : c){
                Node n = contents.get(o);
                if(n == null) continue;
                removeNode(n,false);
                changed = true;
            }

            if(changed) contents.notifyAll();
        }

        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c){
        boolean changed = false;
        if(this == c) return changed;

        synchronized(contents){
            for(E e : new LinkedList<E>(contents.keySet())){
                if(!c.contains(e)){
                    Node n = contents.get(e);
                    if(n != null){
                        removeNode(n,false);
                        changed = true;
                    }
                }
            }

            if(changed) contents.notifyAll();
        }

        return changed;
    }

    @Override
    public int size(){ synchronized(contents){ return contents.size(); }}

    @Override
    public E take() throws InterruptedException{
        synchronized(contents){
            while(contents.isEmpty()) contents.wait();
            return dequeue();
        }
    }

    @Override
    public Object[] toArray(){
        synchronized(contents){ return toArray(new Object[size()]); }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        synchronized(contents){
            //Estimate size of array; be prepared to see more or fewer elements
            int size = size();
            T[] r = a.length >= size ? a :
                    (T[])java.lang.reflect.Array
                            .newInstance(a.getClass().getComponentType(), size);
            Iterator<E> it = iterator();

            for (int i = 0; i < r.length; i++) {
                if (! it.hasNext()) { // fewer elements than expected
                    if (a != r)
                        return Arrays.copyOf(r, i);
                    r[i] = null; // null-terminate
                    return r;
                }
                r[i] = (T)it.next();
            }
            return it.hasNext() ? finishToArray(r, it) : r;
        }
    }

    //Static helper methods: --------------------------------------------------

    @SuppressWarnings("unchecked")
    private static <T> T[] finishToArray(T[] r, Iterator<?> it) {
        int i = r.length;

        while (it.hasNext()) {
            int cap = r.length;
            if (i == cap) {
                int newCap = ((cap / 2) + 1) * 3;
                if (newCap <= cap) { // integer overflow
                    if (cap == Integer.MAX_VALUE)
                        throw new OutOfMemoryError
                                ("Required array size too large");
                    newCap = Integer.MAX_VALUE;
                }
                r = Arrays.copyOf(r, newCap);
            }
            r[i++] = (T)it.next();
        }
        // trim if overallocated
        return (i == r.length) ? r : Arrays.copyOf(r, i);
    }

    //Private inner classes: --------------------------------------------------

    /**
     * Provides a weak iterator that doesn't check for concurrent modification
     * but also fails elegantly.  A race condition exists when simultaneously
     * iterating over the queue while the queue is being modified, but this is
     * allowable per the Java specification for Iterators.
     * @author Ben Lawry
     */
    private class Itr implements Iterator<E>{
        private Node current;
        private E currentElement;

        private Itr(){
            synchronized(contents){
                current = head;
                if(current != null) currentElement = current.element;
                else currentElement = null;
            }
        }

        @Override
        public boolean hasNext(){
            return currentElement != null;
        }

        @Override
        public E next(){
            if(currentElement == null) throw new NoSuchElementException();

            synchronized(contents){
                E e = currentElement;

                current = current.next;
                if(current == null || !contents.containsKey(current.element)){
                    current = null;
                    currentElement = null;
                }
                else currentElement = current.element;

                return e;
            }
        }

        @Override
        public void remove(){
            synchronized(contents){
                if(current == null || !contents.containsKey(current.element))
                    throw new NoSuchElementException();

                Node n = current;
                current = current.next;
                if(current != null && contents.containsKey(current.element))
                    currentElement = current.element;
                else currentElement = null;

                removeNode(n,true);
            }
        }
    }

    /**
     * This class provides a simple implementation for a node in a double-
     * linked list.  It supports constant-time, in-place removals.
     * @author Ben Lawry
     */
    private class Node{
        private Node(E e){
            element = e;
            prev = null;
            next = null;
        }

        private E element;
        private Node prev, next;

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder("Node[prev.element=");
            if(prev == null) sb.append("null,element=");
            else sb.append(prev.element+",element=");
            sb.append(element+",next.element=");
            if(next == null) sb.append("null]");
            else sb.append(next.element+"]");
            return sb.toString();
        }
    }
}

