function setRpName(event) {
  element = event.target
  if (element.checked) {
    document.getElementById("rp-name").value = element.value
  } else {
    document.getElementById("rp-name").value = ''
  }
}

document.getElementById('loa1-rp').addEventListener('click', setRpName);
document.getElementById('loa2-loa1-rp').addEventListener('click', setRpName);
document.getElementById('forceauthn-noc3-rp').addEventListener('click', setRpName);
document.getElementById('not-signed-by-hub-rp').addEventListener('click', setRpName);
document.getElementById('non-eidas-rp').addEventListener('click', setRpName);
