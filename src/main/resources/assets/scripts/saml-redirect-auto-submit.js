document.forms[0].setAttribute("style", "display: none;");

window.setTimeout(function () {
    document.forms[0].removeAttribute("style");
}, 5000);

window.autoSubmit = function() {
    // Using button.click() rather than form.submit() to allow GA to decorate
    // form action with _ga parameter when required
    var submit = document.getElementById('continue-button');
    if (submit) submit.click();
};

if (window.ga) {
    window.ga(function() {
        window.autoSubmit();
    });
} else {
    window.autoSubmit();
}
