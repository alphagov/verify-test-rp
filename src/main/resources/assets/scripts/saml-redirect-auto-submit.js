document.forms[0].setAttribute("style", "display: none;");

window.setTimeout(function () {
    document.forms[0].removeAttribute("style");
}, 5000);

window.autoSubmit = function() {
    var submit = document.getElementById('continue-button');
    if (submit) submit.click();
};

if (!window.ga) window.autoSubmit();
