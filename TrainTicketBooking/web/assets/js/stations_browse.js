document.addEventListener('DOMContentLoaded', () => {
    const regionSelect = document.querySelector('#regionId');
    const citySelect = document.querySelector('#cityId');

    if (regionSelect) {
        regionSelect.addEventListener('change', async () => {
            const regionId = regionSelect.value;
            const res = await fetch(`/api/cities?regionId=${regionId}`);
            const cities = await res.json();

            citySelect.innerHTML = '<option value="">-- Chọn thành phố --</option>';
            cities.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.cityId;
                opt.textContent = c.name;
                citySelect.appendChild(opt);
            });
        });
    }
});
