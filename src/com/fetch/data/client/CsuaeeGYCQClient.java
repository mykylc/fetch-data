package com.fetch.data.client;

import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.FetchData;
import com.fetch.data.domain.Page;
import com.fetch.data.tools.FetchDataThreadPool;

/**
 * 南方联合产权交易中心/国有产权
 * @author 
 *
 */
public class CsuaeeGYCQClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	public final static String domain = "http://www.csuaee.com.cn";
	public final static String webSite = "/Item/ItemList.aspx?CategoryId=1&ProjectChannel21%3AtxtFinishDate=&ProjectChannel21%3AtxtItemCode=%C3%8F%C3%AE%C3%84%C2%BF%C2%B1%C3%A0%C2%BA%C3%85"
			+ "&ProjectChannel21%3AtxtKeyWord=%C2%B9%C3%98%C2%BC%C3%BC%C3%97%C3%96"
			+ "&__EVENTTARGET=AspNetPager1"
			+ "&__EVENTVALIDATION=%2FwEWCQKVlr7OAQL8k67pCgLmo6vmAwLn4InGDALY4qvZAQKsjtuFBQLdhoHXAwLC%2F62ZCwKn9oHKB1JSmMVcFolv51ycRgBPJaXeAQh7"
			+ "&__VIEWSTATE=%2FwEPDwUKMTYyMjU3MzI1Mg9kFgICAg9kFggCAQ8WAh4EVGV4dAUP5Zu95pyJ5Lqn5p2D57G7ZAICDxYCHwAFD%2BWbveacieS6p%2Badg%2Bexu2QCAw8WAh4LXyFJdGVtQ291bnQCFBYoZg9kFgpmDxUHCS9JdGVtL0dFQQgwMDAxNjQ4Mw1HMzE1R0QxMDAwMjcxCS9JdGVtL0dFQQgwMDAxNjQ4M0Xlub%2FkuJznnIHlub%2FlvJjotYTkuqfnu4%2FokKXmnInpmZDlhazlj7gxMCXogqHmnYPovazorqnlj4rlop7otYTmianogqFC5bm%2F5Lic55yB5bm%2F5byY6LWE5Lqn57uP6JCl5pyJ6ZmQ5YWs5Y%2B4MTAl6IKh5p2D6L2s6K6p5Y%2BK5aKe6LWELi4uZAIBDxUBCjIwMTUtMTItMThkAgIPFQIGMjk0NTk0CeW5v%2BW3nuW4gmQCAw8VAQoyMDE1LTExLTIzZAIEDxUBCjIwMTUtMTEtMjNkAgEPZBYKZg8VBwkvSXRlbS9HRUEIMDAwMTY0NDkNRzMxNUdEMTAwMDI3MAkvSXRlbS9HRUEIMDAwMTY0NDkz5Lit5bGx5biC5bCk5Yip5Y2h5aSp54S26I2v54mp5pyJ6ZmQ5YWs5Y%2B4MjUl6IKh5p2DM%2BS4reWxseW4guWwpOWIqeWNoeWkqeeEtuiNr%2BeJqeaciemZkOWFrOWPuDI1JeiCoeadg2QCAQ8VAQoyMDE1LTEyLTE3ZAICDxUCAjYwCeS4reWxseW4gmQCAw8VAQoyMDE1LTExLTIwZAIEDxUBCjIwMTUtMTEtMjBkAgIPZBYKZg8VBwkvSXRlbS9HRUEIMDAwMTYwNjQPRzMxNUdEMTAwMDI1Mi0yCS9JdGVtL0dFQQgwMDAxNjA2NEXkuK3lsbHph5HmmJ%2Flm63mnpfmnLrlhbfliLbpgKDmnInpmZDlhazlj7gxMDDvvIXogqHmnYPlj4rnm7jlhbPlgLrmnYNC5Lit5bGx6YeR5pif5Zut5p6X5py65YW35Yi26YCg5pyJ6ZmQ5YWs5Y%2B4MTAw77yF6IKh5p2D5Y%2BK55u45YWzLi4uZAIBDxUBCjIwMTUtMTItMTFkAgIPFQIHMzk1Ny44MQnkuK3lsbHluIJkAgMPFQEKMjAxNS0wOC0zMWQCBA8VAQoyMDE1LTA4LTMxZAIDD2QWCmYPFQcJL0l0ZW0vR0VBCDAwMDE2NDMwDUYzMTVHRDEwMDAyNjgJL0l0ZW0vR0VBCDAwMDE2NDMwReW5v%2BS4nOecgeW5v%2BW8mOi1hOS6p%2Be7j%2BiQpeaciemZkOWFrOWPuDEwJeiCoeadg%2Bi9rOiuqeWPiuWinui1hOaJqeiCoULlub%2FkuJznnIHlub%2FlvJjotYTkuqfnu4%2FokKXmnInpmZDlhazlj7gxMCXogqHmnYPovazorqnlj4rlop7otYQuLi5kAgEPFQEKMjAxNS0xMi0wM2QCAg8VAgYyOTQ1OTQJ5bm%2F5bee5biCZAIDDxUBCjIwMTUtMTEtMDZkAgQPFQEKMjAxNS0xMS0wNmQCBA9kFgpmDxUHCS9JdGVtL0dFQQgwMDAxNjE2MA9HMzE1R0QxMDAwMjU1LTIJL0l0ZW0vR0VBCDAwMDE2MTYwMOe%2Fgea6kOWOv%2BW5v%2BS4muiThOeUteaxoOaciemZkOWFrOWPuDEwMO%2B8heiCoeadgzDnv4HmupDljr%2Flub%2FkuJrok4TnlLXmsaDmnInpmZDlhazlj7gxMDDvvIXogqHmnYNkAgEPFQEKMjAxNS0xMS0xNmQCAg8VAgcyMzU3LjcyCemftuWFs%2BW4gmQCAw8VAQoyMDE1LTA5LTE2ZAIEDxUBCjIwMTUtMDktMTZkAgUPZBYKZg8VBwkvSXRlbS9HRUEIMDAwMTY0MzINRzMxNUdEMTAwMDI2NgkvSXRlbS9HRUEIMDAwMTY0MzIw5bm%2F5Lic5bm%2F5Lia546v5L%2Bd56eR5oqA5pyJ6ZmQ5YWs5Y%2B4NDkuMzUl6IKh5p2DMOW5v%2BS4nOW5v%2BS4mueOr%2BS%2FneenkeaKgOaciemZkOWFrOWPuDQ5LjM1JeiCoeadg2QCAQ8VAQoyMDE1LTExLTEwZAICDxUCBjY4Mi4xMQnkuJzojp7luIJkAgMPFQEKMjAxNS0xMC0xNGQCBA8VAQoyMDE1LTEwLTE0ZAIGD2QWCmYPFQcJL0l0ZW0vR0VBCDAwMDE2MjY1DUczMTVHRDEwMDAyNjUJL0l0ZW0vR0VBCDAwMDE2MjY1MeW5v%2BW3nuW4guWQr%2BeHiueJqeS4mueuoeeQhuaciemZkOWFrOWPuDEwMCXogqHmnYMx5bm%2F5bee5biC5ZCv54eK54mp5Lia566h55CG5pyJ6ZmQ5YWs5Y%2B4MTAwJeiCoeadg2QCAQ8VAQoyMDE1LTExLTA0ZAICDxUCCTMyOTIuOTUyMQnlub%2Flt57luIJkAgMPFQEKMjAxNS0xMC0wOWQCBA8VAQoyMDE1LTEwLTA5ZAIHD2QWCmYPFQcJL0l0ZW0vR0VBCDAwMDE2MjY0DUczMTVHRDEwMDAyNjQJL0l0ZW0vR0VBCDAwMDE2MjY0LeW5v%2BW3numRq%2BWQr%2BS%2FoeaBr%2BaKgOacr%2BaciemZkOWFrOWPuDgwJeiCoeadgy3lub%2Flt57pkavlkK%2Fkv6Hmga%2FmioDmnK%2FmnInpmZDlhazlj7g4MCXogqHmnYNkAgEPFQEKMjAxNS0xMS0wM2QCAg8VAgMwLjEJ5bm%2F5bee5biCZAIDDxUBCjIwMTUtMTAtMDhkAgQPFQEKMjAxNS0xMC0wOGQCCA9kFgpmDxUHCS9JdGVtL0dFQQgwMDAxNjI1MQ1RMzE1R0QxMDAwMDA5CS9JdGVtL0dFQQgwMDAxNjI1MTPlub%2FkuJzmmZ%2FkuJbnhafpgqbnianmtYHmnInpmZDlhazlj7g0OC45OSXnmoTogqHmnYMz5bm%2F5Lic5pmf5LiW54Wn6YKm54mp5rWB5pyJ6ZmQ5YWs5Y%2B4NDguOTkl55qE6IKh5p2DZAIBDxUBCjIwMTUtMTAtMzBkAgIPFQIHMzE4OC43NQnkvZvlsbHluIJkAgMPFQEKMjAxNS0wOS0yOWQCBA8VAQoyMDE1LTA5LTI5ZAIJD2QWCmYPFQcJL0l0ZW0vR0VBCDAwMDE2MjUwDUczMTVHRDEwMDAyNjMJL0l0ZW0vR0VBCDAwMDE2MjUwM%2BW5v%2BS4nOaZn%2BS4lueFp%2BmCpueJqea1geaciemZkOWFrOWPuDUxLjAxJeeahOiCoeadgzPlub%2FkuJzmmZ%2FkuJbnhafpgqbnianmtYHmnInpmZDlhazlj7g1MS4wMSXnmoTogqHmnYNkAgEPFQEKMjAxNS0xMC0zMGQCAg8VAgczNDY3LjU0CeS9m%2BWxseW4gmQCAw8VAQoyMDE1LTA5LTI5ZAIEDxUBCjIwMTUtMDktMjlkAgoPZBYKZg8VBwkvSXRlbS9HRUEIMDAwMTYxNzANRzMxNUdEMTAwMDI2MgkvSXRlbS9HRUEIMDAwMTYxNzAt5bm%2F5bee5aSp5oGS5py66L2m5bel5Lia5pyJ6ZmQ5YWs5Y%2B4MjAl6IKh5p2DLeW5v%2BW3nuWkqeaBkuacuui9puW3peS4muaciemZkOWFrOWPuDIwJeiCoeadg2QCAQ8VAQoyMDE1LTEwLTIxZAICDxUCATgJ5bm%2F5bee5biCZAIDDxUBCjIwMTUtMDktMThkAgQPFQEKMjAxNS0wOS0xOGQCCw9kFgpmDxUHCS9JdGVtL0dFQQgwMDAxNjE2OQ1HMzE1R0QxMDAwMjYxCS9JdGVtL0dFQQgwMDAxNjE2OS3lub%2Flt57lpKnmgZLmnLrovablt6XkuJrmnInpmZDlhazlj7gzMCXogqHmnYMt5bm%2F5bee5aSp5oGS5py66L2m5bel5Lia5pyJ6ZmQ5YWs5Y%2B4MzAl6IKh5p2DZAIBDxUBCjIwMTUtMTAtMjFkAgIPFQICMTIJ5bm%2F5bee5biCZAIDDxUBCjIwMTUtMDktMThkAgQPFQEKMjAxNS0wOS0xOGQCDA9kFgpmDxUHCS9JdGVtL0dFQQgwMDAxNjE2Ng1HMzE1R0QxMDAwMjYwCS9JdGVtL0dFQQgwMDAxNjE2NinkuprmtLLljqjljavllYbln47mnInpmZDlhazlj7gxNe%2B8heiCoeadgynkuprmtLLljqjljavllYbln47mnInpmZDlhazlj7gxNe%2B8heiCoeadg2QCAQ8VAQoyMDE1LTEwLTIxZAICDxUCCDM5MjMuOTY0Ceaxn%2BmXqOW4gmQCAw8VAQoyMDE1LTA5LTE4ZAIEDxUBCjIwMTUtMDktMThkAg0PZBYKZg8VBwkvSXRlbS9HRUEIMDAwMTYwNzYPRzMxNUdEMTAwMDI1My0yCS9JdGVtL0dFQQgwMDAxNjA3NjTkuK3lsbHljY7kv6Hnva7kuJrmiL%2FkuqflvIDlj5HmnInpmZDlhazlj7gxMDAl6IKh5p2DNOS4reWxseWNjuS%2Foee9ruS4muaIv%2BS6p%2BW8gOWPkeaciemZkOWFrOWPuDEwMCXogqHmnYNkAgEPFQEKMjAxNS0xMC0yMWQCAg8VAgUyMjgwMAnkuK3lsbHluIJkAgMPFQEKMjAxNS0wOS0wMmQCBA8VAQoyMDE1LTA5LTAyZAIOD2QWCmYPFQcJL0l0ZW0vR0VBCDAwMDE2MTY0DUczMTVHRDEwMDAyNTkJL0l0ZW0vR0VBCDAwMDE2MTY0LeW5v%2BS4nOmBk%2Bi3r%2BS%2FoeaBr%2BWPkeWxleaciemZkOWFrOWPuDIwJeiCoeadgy3lub%2FkuJzpgZPot6%2Fkv6Hmga%2Flj5HlsZXmnInpmZDlhazlj7gyMCXogqHmnYNkAgEPFQEKMjAxNS0xMC0yMGQCAg8VAgcxNDEuMTgyCeW5v%2BW3nuW4gmQCAw8VAQoyMDE1LTA5LTE3ZAIEDxUBCjIwMTUtMDktMTdkAg8PZBYKZg8VBwkvSXRlbS9HRUEIMDAwMTYxNTgNRzMxNUdEMTAwMDI1OAkvSXRlbS9HRUEIMDAwMTYxNTgq5LqR5rWu5biC5Lia5Y2O5YyW5bel5pyJ6ZmQ5YWs5Y%2B4Mjkl6IKh5p2DKuS6kea1ruW4guS4muWNjuWMluW3peaciemZkOWFrOWPuDI5JeiCoeadg2QCAQ8VAQoyMDE1LTEwLTE5ZAICDxUCBjMzNC4zNwnkupHmta7luIJkAgMPFQEKMjAxNS0wOS0xNmQCBA8VAQoyMDE1LTA5LTE2ZAIQD2QWCmYPFQcJL0l0ZW0vR0VBCDAwMDE2MTU5DUczMTVHRDEwMDAyNTcJL0l0ZW0vR0VBCDAwMDE2MTU5KuS6kea1ruW4guS4muWNjuWMluW3peaciemZkOWFrOWPuDIwJeiCoeadgyrkupHmta7luILkuJrljY7ljJblt6XmnInpmZDlhazlj7gyMCXogqHmnYNkAgEPFQEKMjAxNS0xMC0xOWQCAg8VAgUyMzAuNgnkupHmta7luIJkAgMPFQEKMjAxNS0wOS0xNmQCBA8VAQoyMDE1LTA5LTE2ZAIRD2QWCmYPFQcJL0l0ZW0vR0VBCDAwMDE2MDM3DUczMTVHRDEwMDAyNTYJL0l0ZW0vR0VBCDAwMDE2MDM3MOaip%2BW3nuW4gui2iuaWsOi1pOawtOeggeWktOaciemZkOWFrOWPuDUxJeiCoeadgzDmoqflt57luILotormlrDotaTmsLTnoIHlpLTmnInpmZDlhazlj7g1MSXogqHmnYNkAgEPFQEKMjAxNS0xMC0xNWQCAg8VAggxNTM2My41Ngnmoqflt57luIJkAgMPFQEKMjAxNS0wOC0yOGQCBA8VAQoyMDE1LTA4LTI4ZAISD2QWCmYPFQcJL0l0ZW0vR0VBCDAwMDE1MjA3D0czMTVHRDEwMDAyMzItMgkvSXRlbS9HRUEIMDAwMTUyMDcx6ZmG5Liw5biC6YeR5Lq%2F5pyJ6Imy6YeR5bGe5pyJ6ZmQ5YWs5Y%2B4MTAwJeiCoeadgzHpmYbkuLDluILph5Hkur%2FmnInoibLph5HlsZ7mnInpmZDlhazlj7gxMDAl6IKh5p2DZAIBDxUBCjIwMTUtMDktMjFkAgIPFQIHMjMyNy42MwnmsZXlsL7luIJkAgMPFQEKMjAxNS0wMy0yNWQCBA8VAQoyMDE1LTAzLTI1ZAITD2QWCmYPFQcJL0l0ZW0vR0VBCDAwMDE1OTcwD0czMTVHRDEwMDAyNDctMgkvSXRlbS9HRUEIMDAwMTU5NzAv5bm%2F5Lic5Y2X5rKZ5riv5qGl6IKh5Lu95pyJ6ZmQ5YWs5Y%2B4MS43NyXogqHmnYMv5bm%2F5Lic5Y2X5rKZ5riv5qGl6IKh5Lu95pyJ6ZmQ5YWs5Y%2B4MS43NyXogqHmnYNkAgEPFQEKMjAxNS0wOS0xNWQCAg8VAgM5NTUJ5bm%2F5bee5biCZAIDDxUBCjIwMTUtMDgtMThkAgQPFQEKMjAxNS0wOC0xOGQCBA8PFgQeC1JlY29yZGNvdW50AuoDHhBDdXJyZW50UGFnZUluZGV4AgJkZGQ%2FJnXG5SQRoex8iDp0HzxHwfuOjA%3D%3D"
			+ "&header1%3AddlType=New&header1%3AtxtKeyWord="
			+ "&__EVENTARGUMENT=";
	public static int pageSize = 20;	   
	public static int currentPage = 1;
	public final String charset = "GB2312";
	
	// 提取总页数的正则表达式
	public static final Pattern pageCountPattern = Pattern.compile("<td valign=\"bottom\" align=\"left\" nowrap=\"true\" style=\"width:15%;\"><a>共(.*?)条记录</a></td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
										  
	// 提取URL列表的正则表达式
	public static final Pattern urlListPattern = Pattern.compile(
    		"    <td><a href=\"(.*?)\" target=\"_blank\" class=\"special-1\">.*?</a></td>\\s+<td><a href=\".*?\" target=\"_blank\"", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		
	//提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<td width=\"200\" height=\"25\" bgcolor=\"#e1edfe\" align=\"left\">&nbsp;&nbsp;<b>转让标的名称</b></td>\\s+<td width=\"800\" bgcolor=\"#fafafa\" align=\"left\" colspan=\"3\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern namePattern2 = Pattern.compile(
    		"</td><th>项目名称</th><td>(.*?)</td></tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern namePattern3 = Pattern.compile(
    		"<script language=\"javascript\">\\$\\(\"label\\[name='Name'\\]\"\\)\\.html\\(\"(.*?)\"\\);</script>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取price
    private static final Pattern pricePattern = Pattern.compile(
    		"<td width=\"200\" height=\"25\" bgcolor=\"#e1edfe\" align=\"left\">&nbsp;&nbsp;<b>挂牌价格（万元）</b></td>\\s+<td width=\"200\" bgcolor=\"#fafafa\" align=\"left\">\\s*(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern pricePattern2 = Pattern.compile(
    		"</td><th>挂牌价格</th><td>(.*?)</td></tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern pricePattern3 = Pattern.compile(
    		"<script language=\"javascript\">\\$\\(\"label\\[name='Price'\\]\"\\)\\.html\\(\"(.*?)\"\\);</script>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取listing_date end_date值
    private static final Pattern datePattern = Pattern.compile(
    		"<td width=\"200\" height=\"25\" bgcolor=\"#e1edfe\" align=\"left\">&nbsp;&nbsp;<b>挂牌起始日期</b></td>\\s+<td width=\"200\" bgcolor=\"#fafafa\" align=\"left\">(.*?)</td>\\s+<td width=\"200\" height=\"25\" bgcolor=\"#e1edfe\" align=\"left\">&nbsp;&nbsp;<b>挂牌期满日期</b></td>\\s+<td width=\"200\" bgcolor=\"#fafafa\" align=\"left\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
	private static final Pattern datePattern2 = Pattern.compile(
    		"<b><span style=\"font-family: 宋体; background: \\#e1edfe; color: black; font-size: 9pt\">挂牌日期</span></b></p>\\s+</td>\\s+<td width=\"724\" colspan=\"15\" style=\"border-bottom: black 1pt solid; border-left: medium none; padding-bottom: 0cm; padding-left: 5\\.4pt; width: 542\\.75pt; padding-right: 5\\.4pt; background: \\#fafafa; border-top: medium none; border-right: black 1pt solid; padding-top: 0cm\">(.*?)</td>\\s+</tr>\\s+<tr>\\s+<td width=\"125\" style=\"border-bottom: black 1pt solid; border-left: black 1pt solid; padding-bottom: 0cm; padding-left: 5\\.4pt; width: 93\\.4pt; padding-right: 5\\.4pt; background: \\#e1edfe; border-top: medium none; border-right: black 1pt solid; padding-top: 0cm\">\\s+<p class=\"MsoNormal\" style=\"background: \\#e1edfe; text-autospace: \"><b><span style=\"font-family: 宋体; background: \\#e1edfe; color: black; font-size: 9pt\">挂牌期满日期</span></b></p>\\s+</td>\\s+<td width=\"724\" colspan=\"15\" style=\"border-bottom: black 1pt solid; border-left: medium none; padding-bottom: 0cm; padding-left: 5\\.4pt; width: 542\\.75pt; padding-right: 5\\.4pt; background: \\#fafafa; border-top: medium none; border-right: black 1pt solid; padding-top: 0cm\">(.*?)</td>\\s+</tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern datePattern3 = Pattern.compile(
    		"<script language=\"javascript\">\\$\\(\"label\\[name='HandBeginDate'\\]\"\\)\\.html\\(\"(.*?)\"\\);</script>"
    		+ "<script language=\"javascript\">\\$\\(\"label\\[name='FinishDate'\\]\"\\)\\.html\\(\"(.*?)\"\\);</script>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取location industry值
    private static final Pattern locationIndustryPattern = Pattern.compile(
    		"<td width=\"200\" height=\"25\" bgcolor=\"#e1edfe\" align=\"left\">&nbsp;&nbsp;<b>标的所在地区</b></td>\\s+<td width=\"200\" bgcolor=\"#fafafa\" align=\"left\">(.*?)</td>\\s+<td width=\"200\" height=\"25\" bgcolor=\"#e1edfe\" align=\"left\">&nbsp;&nbsp;<b>标的所属行业</b></td>\\s+<td width=\"200\" bgcolor=\"#fafafa\" align=\"left\">(.*?)</td",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
    private static final Pattern locationIndustryPattern2 = Pattern.compile(
    		"<th>行业分类</th><td>(.*?)</td></tr>														<tr><th>项目地点</th><td colspan=\"3\">(.*?)</td></tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage;
			final Page page = getPage(url, pageCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			//log.debug(page.getTotalCount()+"");
			getUrlListByPage(url, urlListPattern, domain, charset);
			while (page.hasNextPage()) {
				semp.acquire();
				final int nextPage = page.getNextPage();
				page.setCurrentPage(nextPage);
				FetchDataThreadPool.exec.execute(new Runnable() {
					@Override
					public void run() {
						try {
							String urlPage = domain + webSite + nextPage;
							getUrlListByPage(urlPage, urlListPattern, domain, charset);
						} catch (Exception e) {
							log.error("启动线程报错："+e.getMessage(), e);
						} finally {
							semp.release();
						}
					}
				});
			}
			while (true) {
				if (semp.availablePermits()==10) {
						break;
				}
			}
		} catch (Exception e) {
			log.error("获取CsuaeeGYCQClient网站报错："+e.getMessage(), e);
			throw e;
		}
	}
    
    @Override
   	public FetchData parseObject(String content, String pageUrl) throws Exception {
   		FetchData data = new FetchData();
   		data.setPageUrl(pageUrl);
   		data.setCountry("中国");
   		log.debug(content);
   		Matcher nameMatcher = namePattern.matcher(content);
   		Matcher nameMatcher2 = namePattern2.matcher(content);
   		Matcher nameMatcher3 = namePattern3.matcher(content);
   		if (nameMatcher.find()) {
   			log.debug(nameMatcher.group(1).replaceAll("&nbsp;", ""));
   			data.setName(nameMatcher.group(1).replaceAll("&nbsp;", ""));
   		}else if (nameMatcher2.find()) {
   			log.debug(nameMatcher2.group(1).replaceAll("&nbsp;", ""));
   			data.setName(nameMatcher2.group(1).replaceAll("&nbsp;", ""));
   		}else if (nameMatcher3.find()) {
   			log.debug(nameMatcher3.group(1).replaceAll("&nbsp;", ""));
   			data.setName(nameMatcher3.group(1).replaceAll("&nbsp;", ""));
   		}
   		
   		Matcher priceMatcher = pricePattern.matcher(content);
   		Matcher priceMatcher2 = pricePattern2.matcher(content);
   		Matcher priceMatcher3 = pricePattern3.matcher(content);
   		if (priceMatcher.find()) {
   			log.debug(priceMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
   			data.setPrice(priceMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
   		}else if (priceMatcher2.find()) {
   			log.debug(priceMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
   			data.setPrice(priceMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
   		}else if (priceMatcher3.find()) {
   			log.debug(priceMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
   			data.setPrice(priceMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
   		}
   		Matcher dateMatcher = datePattern.matcher(content);
   		Matcher dateMatcher2 = datePattern2.matcher(content);
   		Matcher dateMatcher3 = datePattern3.matcher(content);
   		if (dateMatcher.find()) {
   			log.debug(dateMatcher.group(1).replaceAll("&nbsp;", "")+"="+dateMatcher.group(2).replaceAll("&nbsp;", ""));
   			data.setListingDate(dateMatcher.group(1).replaceAll("&nbsp;", ""));
   			data.setEndDate(dateMatcher.group(2).replaceAll("&nbsp;", ""));
   		}else if (dateMatcher2.find()) {
   			log.debug(dateMatcher2.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "")+"="+dateMatcher2.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
   			data.setListingDate(dateMatcher2.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("\\s+", ""));
   			data.setEndDate(dateMatcher2.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("\\s+", ""));
   		}else if (dateMatcher3.find()) {
   			log.debug(dateMatcher3.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "")+"="+dateMatcher3.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
   			data.setListingDate(dateMatcher3.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("\\s+", ""));
   			data.setEndDate(dateMatcher3.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("\\s+", ""));
   		}
   		Matcher locationIndustryMatcher = locationIndustryPattern.matcher(content);
   		Matcher locationIndustryMatcher2 = locationIndustryPattern2.matcher(content);
   		if (locationIndustryMatcher.find()) {
   			log.debug(locationIndustryMatcher.group(1).replaceAll("&nbsp;", "")+"="+locationIndustryMatcher.group(2).replaceAll("&nbsp;", ""));
   			data.setLocation(locationIndustryMatcher.group(1).replaceAll("&nbsp;", ""));
   			data.setIndustry(locationIndustryMatcher.group(2).replaceAll("&nbsp;", ""));
   		}else if (locationIndustryMatcher2.find()) {
   			log.debug(locationIndustryMatcher2.group(1).replaceAll("&nbsp;", "")+"="+locationIndustryMatcher2.group(2).replaceAll("&nbsp;", ""));
   			data.setIndustry(locationIndustryMatcher2.group(1).replaceAll("&nbsp;", ""));
   			data.setLocation(locationIndustryMatcher2.group(2).replaceAll("&nbsp;", ""));
   		}
   		return data;
   	}
	
}
