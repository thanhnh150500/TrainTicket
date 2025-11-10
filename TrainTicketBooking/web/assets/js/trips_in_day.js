document.addEventListener('DOMContentLoaded', () => {
    setInterval(() => {
        const url = new URL(window.location.href);
        fetch(url)
                .then(res => res.text())
                .then(html => {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    const table = doc.querySelector('table');
                    if (table) {
                        document.querySelector('table').replaceWith(table);
                    }
                });
    }, 60000); // refresh mỗi 60 giây
});
