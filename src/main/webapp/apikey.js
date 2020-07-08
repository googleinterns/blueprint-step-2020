function getApiKey() {
  fetch('/secret-manager')
    .then((response) => {
      console.log(response);
      document.getElementById('api-key').innerHTML = response;
    });
}
