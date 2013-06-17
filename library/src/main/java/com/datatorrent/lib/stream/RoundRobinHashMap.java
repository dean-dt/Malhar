/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.datatorrent.lib.stream;

import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.api.annotation.InputPortFieldAnnotation;
import com.datatorrent.api.annotation.OutputPortFieldAnnotation;
import com.datatorrent.lib.util.BaseKeyValueOperator;
import java.util.HashMap;

/**
 *
 * Creates a HashMap tuple from incoming tuples. If keys[] are set, then each incoming tuple is the value of the key (in-order) till all keys get a value.
 * Once all keys are assigned values, the tuple (HashMap) is emitted, the process of assigning values starts again<p>
 * This is a stateful operator as it waits across window boundary to complete the HashTable<br>
 * <b>Port</b>:<br>
 * <b>data</b>: expects V<br>
 * <b>map</b>: emits HashMap&lt;K,v&gt;<br>
 * <br>
 * <b>Properties</b>:<br>
 * <b>keys[]</b>: Set of keys to insert in the output tuple</b>
 * <b>Specific compile time checks</b>: None<br>
 * <b>Specific run time checks</b>: None<br>
 * <p>
 * <b>Benchmarks</b>: Blast as many tuples as possible in inline mode<br>
 * <table border="1" cellspacing=1 cellpadding=1 summary="Benchmark table for DevNull operator template">
 * <tr><th>In-Bound</th><th>Out-bound</th><th>Comments</th></tr>
 * <tr><td><b>&gt; 25 million tuples/s</td><td>One tuple (HashMap) emitted for N (N=3) incoming tuples, where N is the number of keys</td>
 * <td>In-bound rate is the main determinant of performance</td></tr>
 * </table><br>
 * <p>
 * <b>Function Table (K=String, V=Integer), keys = [a,b,c]</b>:
 * <table border="1" cellspacing=1 cellpadding=1 summary="Function table for DevNull operator template">
 * <tr><th rowspan=2>Tuple Type (api)</th><th>In-bound (<i>data</i>::process)</th><th>No Outbound port</th></tr>
 * <tr><th><i>data</i>(V)</th><th><i>map</i>(HashMap&gt;K,V&lt;</th></tr>
 * <tr><td>Begin Window (beginWindow())</td><td>N/A</td><td>N/A</td></tr>
 * <tr><td>Data (process())</td><td>2</td><td></td></tr>
 * <tr><td>Data (process())</td><td>66</td><td></td></tr>
 * <tr><td>Data (process())</td><td>5</td><td>{a=2,b=66,c=5}</td></tr>
 * <tr><td>Data (process())</td><td>2</td><td></td></tr>
 * <tr><td>Data (process())</td><td>-1</td><td></td></tr>
 * <tr><td>Data (process())</td><td>3</td><td>{a=2,b=-1,c=3}</td></tr>
 * <tr><td>Data (process())</td><td>12</td><td></td></tr>
 * <tr><td>Data (process())</td><td>13</td><td></td></tr>
 * <tr><td>Data (process())</td><td>5</td><td>{a=12,b=13,c=5}</td></tr>
 * <tr><td>End Window (endWindow())</td><td>N/A</td><td>N/A</td></tr>
 * </table>
 * <br>
 */

public class RoundRobinHashMap<K, V> extends BaseKeyValueOperator<K, V>
{
  protected K[] keys;
  protected int cursor = 0;
  private HashMap<K,V> otuple;

  @InputPortFieldAnnotation(name = "data")
  public final transient DefaultInputPort<V> data = new DefaultInputPort<V>(this)
  {
    /**
     * Emits key, key/val pair, and val based on port connections
     */
    @Override
    public void process(V tuple)
    {
      if (keys.length == 0) {
        return;
      }
      if (cursor == 0) {
        otuple = new HashMap<K,V>();
      }
      otuple.put(keys[cursor], tuple);
      if (++cursor >= keys.length) {
        map.emit(otuple);
        cursor = 0;
        otuple = null;
      }
    }
  };
  @OutputPortFieldAnnotation(name = "map")
  public final transient DefaultOutputPort<HashMap<K, V>> map = new DefaultOutputPort<HashMap<K, V>>(this);

  public void setKeys(K[] keys) {
    this.keys = keys;
  }
}