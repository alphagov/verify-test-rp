 document.addEventListener('DOMContentLoaded', function(){
    var crossGovGaTrackerId = document.getElementById("cross-gov-ga-tracker-id");
    if (crossGovGaTrackerId && crossGovGaTrackerId.innerText) {
        (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
            (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
            m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
        })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

        var domainList = [
            "www.gov.uk",
            "localhost"
        ];

        window.ga("create", crossGovGaTrackerId.innerText, "auto", "govuk_shared", {"allowLinker": true});
        window.ga("govuk_shared.require", "linker");
        window.ga("govuk_shared.linker.set", "anonymizeIp", true);
        window.ga("govuk_shared.linker:autoLink", domainList, false, true);
        window.ga("govuk_shared.send", "pageview");
    }
 });
