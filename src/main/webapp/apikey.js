function getApiKey() {
  fetch('/secret-manager')
    .then((response) => (response.json()))
    .then((apikey) => {
      console.log(apikey);
      document.getElementById('api-key').innerHTML = apikey;
    });
}
