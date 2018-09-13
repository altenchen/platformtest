package enn.platform.opentsdbtest;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.SECONDS;

/*
写：
10.39.42.5  cdhprod-c-1   4242
10.39.42.3  cdhprod-c-2   4242
10.39.42.4  cdhprod-c-3   4242
负载均衡： 10.39.41.56:4444


读：
10.39.42.6  cdhprod-c-4    4242
10.39.42.7  cdhprod-c-5    4242
负载均衡： 10.39.41.55:4444
*/

public class TSDBPerfToolTest {
  // OpenTSDB IP地址
  private String address;
  // OpenTSDB 端口
  private int port;
  // 每个DataPoint中Tag个数
  private int tagNumber;
  // 每秒发送的DataPoint数量
  private int tps;
  // 每个PUT请求中DataPoint的数量
  private int batchNumber;
  // 并发线程数
  private int threads;
  // 是否使用同步写入HBase功能
  private boolean sync;
  // 模拟数据的起始日期
  private String startDate;
  // 运行时间
  private int runTime;
  // 超时时间
  private int SYNC_TIMEOUT_MS;
  // 重试次数
  private int MAX_TRY;
  // URL
  private  String writeUrl;
  // 结束时间
  private String endDate;



//  private static AtomicLong LOOP = new AtomicLong(getTimestamp("2000-01-01 00:00:00") / 1000);
  private static AtomicLong LOOP = new AtomicLong(getCurrentTimestamp() / 1000);

  private AtomicLong successCount = new AtomicLong();
  private AtomicLong lastSuccessCount = new AtomicLong();
  private final AtomicLong sumTime = new AtomicLong();
  private final DataProducer dataProducer = new DataProducer();
  private final ArrayList<Thread> PUT_THREADS = new ArrayList<Thread>();
  private static CloseableHttpClient httpClient;

  private PropertiesUtil prop = new PropertiesUtil("/common.properties");

  static {

    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(200);
    cm.setDefaultMaxPerRoute(20);
    cm.setDefaultMaxPerRoute(50);
    httpClient = HttpClients.custom().setConnectionManager(cm).build();
  }

  //返回时间戳
  public static long getCurrentTimestamp() {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date = df.format(new Date());
    long timeStamp = System.currentTimeMillis();
    try {
      Date timeStampFormat = df.parse(date);
      return timeStampFormat.getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return timeStamp;
  }

  //返回"yyyy-MM-dd HH:mm:ss"格式
  public static String getStringTime() {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date = df.format(new Date());
    return date;
  }


  public static int getConf(String propetry, int defaultValue) {
    int ret = defaultValue;
    String value = System.getenv(propetry);
    if (value != null && !value.isEmpty()) {
      try {
        ret = Integer.valueOf(value);
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(-1);
      }
    }
    return ret;
  }

  public static String getConf(String propetry, String defaultValue) {
    String ret = defaultValue;
    String value = System.getenv(propetry);
    if (value != null && !value.isEmpty()) {
      ret = value;
    }
    return ret;
  }

  public static boolean getConf(String propetry, boolean defaultValue) {
    boolean ret = defaultValue;
    String value = System.getenv(propetry);
    if (value != null && !value.isEmpty()) {
      try {
        ret = Boolean.parseBoolean(value);
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(-1);
      }
    }
    return ret;
  }

  /**
   * 初始化配置文件
   */
  private void initConf() {
    //类内自定义属性名
//    this.tagNumber = getConf("TAG_NUMBER", 3);
//    this.tps = getConf("TPS", 50000);
//    this.batchNumber = getConf("BATCH_NUMBER", 60);  //每个 PUT 请求中数据点的数量
//    this.threads = getConf("THREADS", 8);
//    this.port = getConf("PORT", 4444);
//    this.address = getConf("ADDRESS", "10.39.41.56");
//    this.MAX_TRY = getConf("MAX_TRY", 4);
//    this.SYNC_TIMEOUT_MS = getConf("SYNC_TIMEOUT_MS", 2 * 60 * 1000);
//    this.sync = getConf("SYNC", false);
////    this.startDate = getConf("START_DATE", "2000-01-01 00:00:00");
//    this.startDate = getConf("START_DATE", getStringTime());
//
//    this.runTime = getConf("RUN_TIME", 60);
//    LOOP = new AtomicLong(getTimestamp(this.startDate) / 1000);
//    this.writeUrl =
//        "http://" + this.address + ":" + this.port + "/api/put/" + (this.sync ? "?sync&" : "?") //是否同步写入 Hbase
//            + "sync_timeout=" + this.SYNC_TIMEOUT_MS;   //PUT操作的超时时间限制

    //配置文件中自定义属性名
    this.tagNumber = Integer.valueOf(prop.getProperty("TAG_NUMBER"));
    this.tps = Integer.valueOf(prop.getProperty("TPS"));
    this.batchNumber = Integer.valueOf(prop.getProperty("BATCH_NUMBER"));
    this.threads = Integer.valueOf(prop.getProperty("THREADS"));
    this.port = Integer.valueOf(prop.getProperty("PORT"));
    this.address = prop.getProperty("ADDRESS");
    this.MAX_TRY = Integer.valueOf(prop.getProperty("MAX_TRY"));
    this.SYNC_TIMEOUT_MS = Integer.valueOf(prop.getProperty("SYNC_TIMEOUT_MS"));
    this.sync = Boolean.getBoolean(prop.getProperty("SYNC"));


    this.startDate = getStringTime();

    this.endDate = getEndDate();


    this.runTime = Integer.valueOf(prop.getProperty("RUN_TIME"));
    LOOP = new AtomicLong(getTimestamp(this.startDate) / 1000);
    this.writeUrl =
            "http://" + this.address + ":" + this.port + "/api/put/" + (this.sync ? "?sync&" : "?") //是否同步写入 Hbase
                    + "sync_timeout=" + this.SYNC_TIMEOUT_MS;   //PUT操作的超时时间限制

    System.out.println("RUN_TIME:" + this.runTime);
    System.out.println("TAG_NUMBER:" + this.tagNumber);
    System.out.println("TPS:" + this.tps);
    System.out.println("BATCH_NUMBER:" + this.batchNumber);
    System.out.println("THREADS:" + this.threads);
    System.out.println("ADDRESS:" + this.address);
    System.out.println("PORT:" + this.port);
    System.out.println("SYNC_TIMEOUT_MS:" + this.SYNC_TIMEOUT_MS);
    System.out.println("MAX_TRY:" + this.MAX_TRY);
    System.out.println("SYNC:" + this.sync);
    System.out.println("START_DATE:" + this.startDate);
    System.out.println("END_DATE:" + this.endDate);
  }

  /**
   * 模拟数据的生产者
   */
  class DataProducer {
    private final BlockingQueue<String> flowQueue = new ArrayBlockingQueue<String>(5000);
    private final BlockingQueue<String> dataQueue = new ArrayBlockingQueue<String>(5000);
    private final BlockingQueue<String> retryQueue = new ArrayBlockingQueue<String>(5000);
    ScheduledExecutorService throttling = Executors.newScheduledThreadPool(1);
    private final List<Thread> producerThreads = new ArrayList<Thread>();

    public void start() {
      // 模拟数据发生器
      startProducerThreads();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e1) {
      }

      throttling.scheduleAtFixedRate(() -> {
        for (int i = 0; i < tps; i += batchNumber) {
          String elem = dataQueue.peek();
          if (elem == null) {
            System.out.println("Data producer too low");
            break;
          } else {
            if (flowQueue.offer(elem)) {
              try {
                dataQueue.take();
              } catch (InterruptedException e) {
                break;
              }
            }
          }
        }
      }, 0, 1, SECONDS);
    }

    /**
     * 模拟数据的生产者线程
     */
    private void startProducerThreads() {
      for (int i = 0; i < 4; ++i) {
        Thread thread = new Thread(() -> {
          // 基础数据模型
          ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
          for (int b = 0; b < batchNumber; ++b) {
            Map<String, String> tags = new HashMap<String, String>();
            for (int t = 0; t < tagNumber; ++t) {
              tags.put("tagk" + t, "tagv" + t);
            }
            dataPoints.add(new DataPoint("temp_city_" + b, 0L, 25.2, tags));
          }
          while (true) {
            try {
              String data = null;
              if (retryQueue.size() > 0) {
                data = retryQueue.poll(0, TimeUnit.MILLISECONDS);
              }
              if (data != null) {
                dataQueue.put(data);
              } else {
                long loop = LOOP.getAndAdd(1);
                for (int b = 0; b < batchNumber; b++) {
                  DataPoint dataPoint = dataPoints.get(b);
                  // 重新修改时间戳
                  dataPoint.timestamp = loop;
                }
                Gson gson = new Gson();
                dataQueue.put(gson.toJson(dataPoints));
              }
            } catch (Exception e) {
            }
          }
        });
        thread.setDaemon(true);
        thread.start();
        producerThreads.add(thread);
      }
    }

    public String nextData() throws InterruptedException {
      return flowQueue.poll(Long.MAX_VALUE, SECONDS);
    }

    public void retry(String data) throws InterruptedException {
      retryQueue.put(data);
      if (retryQueue.size() > 1000) {
        System.out.println("Retry queue too large : " + retryQueue.size());
      }
    }
  }

  /**
   * 模拟数据的消费者线程
   */
  public void startConsumerThreads() {
    dataProducer.start();
    for (int i = 0; i < this.threads; ++i) {
      Thread thread = new Thread(() -> {
        while (true) {
          try {
            String data = dataProducer.nextData();
            int t = 0;
            for (; t < MAX_TRY; ++t) {
              //try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(writeUrl);
                StringEntity eStringEntity = new StringEntity(data, "utf-8");
                eStringEntity.setContentType("application/json");
                httpPost.setEntity(eStringEntity);
                long start = System.currentTimeMillis();
                HttpResponse response = httpClient.execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200 && statusCode != 204) {
                  System.err.println("Request failed! " + response.getStatusLine());
                  Thread.sleep(100);
                } else {
                  successCount.addAndGet(batchNumber);
                  long time = System.currentTimeMillis() - start;
                  //System.out.println("time=" + time);
                  sumTime.addAndGet(time);
                  break;
                }
              }
            //}
            if (t == MAX_TRY) {
              dataProducer.retry(data);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
      thread.setDaemon(true);
      thread.start();
      PUT_THREADS.add(thread);
    }
  }

  /**
   * 获取当前时间戳
   * @param dateStr 字符串格式日期
   * @return
   */
  public static long getTimestamp(String dateStr) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date;
    try {
      date = format.parse(dateStr);
      return date.getTime();
    } catch (ParseException e) {
      e.printStackTrace();
      System.exit(-1);
    }
    return System.currentTimeMillis();
  }

  /**
   * 获取结束日期
   * @return
   */
  public String getEndDate() {
    GregorianCalendar gc=new GregorianCalendar();
    try {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String date = df.format(new Date());
//      System.out.println("date:" + date);
      gc.setTime( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date));
      //秒级相加
      int runtime = 300;
      gc.add(13, +runtime);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(gc.getTime());
  }

  /**
   * 开始测试
   * @throws InterruptedException
   */
  public void startTest() throws InterruptedException {
    startConsumerThreads();
    int printTime = 10 * 1000;
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() < (startTime + this.runTime * 1000)) {
      Thread.sleep(printTime);
      //打印测试结果
      printResult(printTime);
    }
  }

  /**
   * 封装测试结果 供startTest()方法调用并打印
   * @param printTime 打印时间 每隔 10s 在控制台打印一次
   */
  private void printResult(int printTime) {
    Long nowSumTime = sumTime.get();
    Long nowSuccess = successCount.get();
    Long currSuccess = nowSuccess - lastSuccessCount.get();
    Long tpr = currSuccess == 0 ? 0 : ((nowSumTime * this.batchNumber) / nowSuccess);
    System.out.println(
        "total success: " + nowSuccess.longValue() + " recent_tps: " + currSuccess * 1000 / printTime
            + " time_per_request: " + tpr + "ms");
    lastSuccessCount.set(nowSuccess);
  }

  public static void main(String[] args) throws InterruptedException {
    TSDBPerfToolTest tpt = new TSDBPerfToolTest();
    tpt.initConf();
    tpt.startTest();
    System.exit(0);
  }
}