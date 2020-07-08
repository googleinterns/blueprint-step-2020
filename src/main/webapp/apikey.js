function getApiKey() {
  fetch('/secret-manager')
    .then((apikey) => {
      console.log(apikey);
      document.getElementById('api-key').innerHTML = apikey;
    });
}
