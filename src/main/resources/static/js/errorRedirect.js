/**
 * Triggers a redirect to the error page.
 */
function redirectErrorPage() {
    window.location.href = contextPath + "/error";
}

export {redirectErrorPage}
