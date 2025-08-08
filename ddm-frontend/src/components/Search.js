import React, { useState } from "react";
import {Button, TextField, Box, Container, MenuItem} from '@mui/material';

function Search() {
  // Koji tip pretrage je aktivan
  const [searchType, setSearchType] = useState("basic"); // basic, knn, geo, advanced

  // Zajednički filteri za sve pretrage
  const [filters, setFilters] = useState({
    employeeFullName: "",
    incidentSeverity: "",
    affectedOrganizationAddress: "",
    securityOrganizationAddress: "",
    text: "",
    fullText: "",
    // za geo i advanced možeš dodati posebna polja
  });

  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  function handleFilterChange(e) {
    const { name, value, type, checked } = e.target;
    setFilters((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  }

  async function handleSearch() {
    setLoading(true);
    // Pripremi payload prema tipu pretrage
    let payload = {};

    switch (searchType) {
      case "basic":
        payload = { type: "basic", ...filters };
        break;
      case "knn":
        payload = { type: "knn", ...filters };
        break;
      case "geo":
        // Dodaj geo-specifična polja ako ih imaš
        payload = { type: "geo", ...filters };
        break;
      case "advanced":
        // Dodaj polja za advanced pretragu
        payload = { type: "advanced", ...filters };
        break;
    }

    try {
      const response = await fetch("/api/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!response.ok) throw new Error("Greška prilikom pretrage");
      const data = await response.json();
      setResults(data);
    } catch (err) {
      alert(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ maxWidth: 1400, margin: "auto", padding: 20 }}>
      <h1>Pretraga</h1>

      {/* Izbor tipa pretrage */}
      <div style={{ marginBottom: 20 }}>
        <label>
          <input
            type="radio"
            name="searchType"
            value="basic"
            checked={searchType === "basic"}
            onChange={(e) => setSearchType(e.target.value)}
          />
          Basic Search
        </label>
        <label style={{ marginLeft: 20 }}>
          <input
            type="radio"
            name="searchType"
            value="knn"
            checked={searchType === "knn"}
            onChange={(e) => setSearchType(e.target.value)}
          />
          KNN Search
        </label>
        <label style={{ marginLeft: 20 }}>
          <input
            type="radio"
            name="searchType"
            value="geo"
            checked={searchType === "geo"}
            onChange={(e) => setSearchType(e.target.value)}
          />
          Geolocation Search
        </label>
        <label style={{ marginLeft: 20 }}>
          <input
            type="radio"
            name="searchType"
            value="advanced"
            checked={searchType === "advanced"}
            onChange={(e) => setSearchType(e.target.value)}
          />
          Advanced Search
        </label>
      </div>

      {/* Filteri */}
      <div style={{ background: "#f8faff", padding: 20, borderRadius: 6 }}>
        <div style={{ marginTop: 10, display: searchType === "basic" ? "block" : "none" }} >
            <TextField
                      label="Employee fullname"
                      name="employeeFullName"
                      variant="filled"
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 300 }}
                      value={filters.employeeName}
                      onChange={handleFilterChange}
                      required
                    />
            <TextField
                      label="Incident severity"
                      name="incidentSeverity"
                      variant="filled"
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.incidentSeverity}
                      onChange={handleFilterChange}
                      required
                    >
                <MenuItem value="niska">niska</MenuItem>
                <MenuItem value="srednja">srednja</MenuItem>
                <MenuItem value="visoka">visoka</MenuItem>
                <MenuItem value="kriticna">kriticna</MenuItem>
            </TextField>

          <TextField
                      label="Affected organization"
                      name="affectedOrganizationName"
                      variant="filled"
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.affectedOrganizationName}
                      onChange={handleFilterChange}
                      required
                    />
          <TextField
                      label="Security organization"
                      name="securityOrganizationName"
                      variant="filled"
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.securityOrganizationName}
                      onChange={handleFilterChange}
                      required
                    />
        <TextField
                      label="Text"
                      name="text"
                      variant="filled"
                      sx={{ mb: 3 }}
                      fullWidth
                      style={{ marginRight: 10}}
                      value={filters.text}
                      onChange={handleFilterChange}
                      required
                    />

   
      </div>
      <div style={{ marginTop: 10, display: searchType === "knn" ? "block" : "none" }} >
            <TextField
                      label="Full-Text"
                      name="fullText"
                      variant="filled"
                      sx={{ mb: 3 }}
                      fullWidth
                      style={{ marginRight: 10}}
                      value={filters.fullText}
                      onChange={handleFilterChange}
                      required
                    />
      </div>
      <div style={{ marginTop: 10, display: searchType === "geo" ? "block" : "none" }} >
            <TextField
                      label="Address"
                      name="address"
                      variant="filled"
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.adress}
                      onChange={handleFilterChange}
                      required
                    />
            <TextField
                      type="number"
                      label="Radius"
                      name="radius"
                      variant="filled"
                      sx={{ mb: 3 }}
                      style={{ marginRight: 10, width: 300  }}
                      value={filters.radius}
                      onChange={handleFilterChange}
                      required
                    />
                    
      </div>

      <Button variant="contained" color="primary"
        onClick={handleSearch}
        disabled={loading}
        style={{ marginTop: 15, padding: "8px 15px" }}>
        {loading ? "Pretražujem..." : "Search"}
      </Button>

      {/* Rezultati */}
      <div style={{ marginTop: 30 }}>
        <h2>{results.length} Results</h2>
        {results.map((r, i) => (
          <div
            key={i}
            style={{
              padding: 15,
              marginBottom: 15,
              border: "1px solid #ddd",
              borderRadius: 5,
              background: "white",
            }}
          >
            <h3 style={{ color: "#3366cc", cursor: "pointer" }}>{r.title}</h3>
            <small>{r.type}</small>
            <p>
              <b>Employee:</b> {r.employeeFullName} <br />
              <b>Security organization:</b> {r.securityOrganizationName} <br />
              <b>Affected organization:</b> {r.affectedOrganizationName} <br />
              <b>Affected organization address:</b> {r.affectedOrganizationAddress}
            </p>
            <p>{r.summary}</p>
            <Button variant="contained" color="primary"
              onClick={() => window.open(r.downloadUrl, "_blank")}
              style={{ float: "right" }}
            >
              DOWNLOAD
            </Button>
          </div>
        ))}
      </div>
    </div>
</div>
  );
}

export default Search;