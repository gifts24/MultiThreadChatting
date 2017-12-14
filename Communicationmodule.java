package javabook.ch14;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MultiChatServer {

   // 서버 소켓과 클라이언트 연결 소켓
   private ServerSocket ss = null;
   private Socket s = null;

   // 연결된 클라이언트 스레드를 관리하는 ArrayList
   ArrayList <ChatThread> chatlist = new ArrayList 
                                                                <ChatThread>( );

   // 멀티챗 메인 프로그램부
   public void start( ) {
      try {
         // 서버 소켓 생성
         ss = new ServerSocket(8888);
         System.out.println("server start");

         // 무한루프를 돌면서 클라이언트 연결을 기다림
         while(true) {
            s = ss.accept( );
            // 연결된 클라이언트에서 스레드 클래스 생성
            ChatThread chat = new ChatThread( );
            // 클라이언트 리스트 추가
            chatlist.add(chat);
            // 스레드 시작
            chat.start( );
         }
      } catch(Exception e) {
         // System.out.println(e);
         System.out.println("[MultiChatServer]start( )Exp 발생!!");
      }
   }

   public static void main(String[ ] args) {
      MultiChatServer server = new MultiChatServer( );
      server.start( );
   }

   // 연결된 모든 클라이언트에 메시지 중계
   void msgSendAll(String msg) {
      for(ChatThread ct : chatlist) {
         ct.outMsg.println(msg);
      }
   }

   // 각 클라이언트 관리를 위한 스레드 클래스
   class ChatThread extends Thread {

      // 수신 메시지와 파싱 메시지 처리하는 변수 선언
      String msg;
      String[ ] rmsg;

      // 입·출력 스트림 생성
      private BufferedReader inMsg = null;
      private PrintWriter outMsg = null;

      public void run( ) {

         boolean status = true;
         System.out.println("##ChatThread start...");
         try {
            // 입·출력 스트림 생성
            inMsg = new BufferedReader
                  (new InputStreamReader(s.getInputStream( )));
            outMsg = new PrintWriter(s.getOutputStream( ), true);

            // 상태정보가 true면 루프를 돌면서 사용자한테서 수신된 메시지 처리
            while(status) {
               // 수신된 메시지를 msg 변수에 저장
               msg = inMsg.readLine( );
               // ’/’ 구분자를 기준으로 메시지를 문자열 배열로 파싱
               rmsg = msg.split("/");

               // 파싱된 문자열 배열의 두 번째 요소값에 따라 처리
               // 로그아웃 메시지일 때
if(rmsg[1].equals("logout")) {
                 chatlist.remove(this);
                 msgSendAll("server/" + rmsg[0] + "님이 종료했습니다.");
                 // 해당 클라이언트 스레드 종료로 status를 false로 설정
                 status = false;
              }
              // 로그인 메시지일 때
              else if(rmsg[1].equals("login")) {
                 msgSendAll("server/" + rmsg[0] + "님이 로그인했습니다.");
              }
              // 그 밖의 일반 메시지일 때
              else {
                 msgSendAll(msg);
              }
           } // while 종료
   // 루프를 벗어나면 클라이언트 연결이 종료되므로 스레드 인터럽트됨
           this.interrupt( );
           System.out.println("##" + this.getName( ) + "stop!!");
        } catch(IOException e) {
           chatlist.remove(this);
           // e.printStackTrace( );
           System.out.println("[ChatThread]run( ) IOException 발생!!");
        }
     }
  }

}

