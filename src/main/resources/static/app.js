var stompClient = null;

function connect() {
  var socket = new SockJS('http://localhost:8080/ws');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    // 특정 토픽을 구독
    stompClient.subscribe('/topic/refreshToken', function (response) {
      const tokenResponse = JSON.parse(response.body);
      if (tokenResponse.status === 200) {
        localStorage.setItem('authToken', tokenResponse.newAccessToken);
        alert('토큰이 성공적으로 재발급 되었습니다.');
      } else {
        alert('토큰 재발급 실패: ' + tokenResponse.message);
      }
    });
  });
}

function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect();
    console.log("Disconnected");
  }
}

function sendRefreshTokenRequest() {
  const token = localStorage.getItem('authToken');
  if (token) {
    stompClient.send("/app/token/refresh", {}, JSON.stringify({ accessToken: token }));
  } else {
    alert("No token available. Please login.");
  }
}

document.getElementById('refreshToken1').addEventListener('click', function() {
  sendRefreshTokenRequest();
});

window.onload = connect;