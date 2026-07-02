import { useState } from 'react'

const API_BASE = '/wifi-parameter'

const ENCRYPTION_TYPES = [
  { value: 'OPEN', label: 'Open' },
  { value: 'WEP', label: 'WEP' },
  { value: 'WPA_PSK', label: 'WPA-PSK' },
  { value: 'WPA2_PSK', label: 'WPA2-PSK' },
  { value: 'WPA3_SAE', label: 'WPA3-SAE' },
  { value: 'WPA2_ENTERPRISE', label: 'WPA2-Enterprise' },
]

const WIFI_BANDS = [
  { value: 'BAND_2_4_GHZ', label: '2.4 GHz' },
  { value: 'BAND_5_GHZ', label: '5 GHz' },
]

export default function App() {
  const [cpeId, setCpeId] = useState('')
  const [config, setConfig] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [editMode, setEditMode] = useState(false)

  const [form, setForm] = useState({
    cpeId: '',
    wifiBand: 'BAND_2_4_GHZ',
    ssid: '',
    encryptionType: 'WPA2_PSK',
    password: '',
  })

  async function handleFetch(e) {
    e.preventDefault()
    if (!cpeId.trim()) return
    setLoading(true)
    setError(null)
    setConfig(null)
    setEditMode(false)
    try {
      const res = await fetch(`${API_BASE}/${encodeURIComponent(cpeId.trim())}`)
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.message || `HTTP ${res.status}`)
      }
      const data = await res.json()
      setConfig(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  function handleEdit() {
    setForm({
      cpeId: config.cpeId,
      wifiBand: config.wifiBand || 'BAND_2_4_GHZ',
      ssid: config.ssid || '',
      encryptionType: config.encryptionType || 'OPEN',
      password: config.password || '',
    })
    setEditMode(true)
  }

  async function handleSave(e) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const res = await fetch(API_BASE, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.message || `HTTP ${res.status}`)
      }
      const data = await res.json()
      setConfig(data)
      setEditMode(false)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  function handleCancel() {
    setEditMode(false)
  }

  return (
    <div className="container">
      <header>
        <h1>WiFi Admin</h1>
        <p className="subtitle">Upravljanje WiFi parametrima CPE uređaja</p>
      </header>

      {/* Search form */}
      <form onSubmit={handleFetch} className="search-form">
        <label htmlFor="cpeId">CPE ID:</label>
        <div className="search-row">
          <input
            id="cpeId"
            type="text"
            value={cpeId}
            onChange={e => setCpeId(e.target.value)}
            placeholder="npr. CPE_001"
            required
          />
          <button type="submit" disabled={loading}>
            {loading ? 'Dohvaćanje...' : 'Dohvati'}
          </button>
        </div>
      </form>

      {error && (
        <div className="error-box">
          <strong>Greška:</strong> {error}
        </div>
      )}

      {/* Display config */}
      {config && !editMode && (
        <div className="config-card">
          <h2>WiFi konfiguracija — {config.cpeId}</h2>
          <table>
            <tbody>
              <tr><td>CPE ID</td><td>{config.cpeId}</td></tr>
              <tr><td>WiFi pojas</td><td>{config.wifiBand}</td></tr>
              <tr><td>SSID</td><td>{config.ssid}</td></tr>
              <tr><td>Enkripcija</td><td>{config.encryptionType || 'OPEN'}</td></tr>
              <tr><td>Lozinka</td><td>{config.password ? '••••••••' : '—'}</td></tr>
            </tbody>
          </table>
          <button className="btn-edit" onClick={handleEdit}>Uredi</button>
        </div>
      )}

      {/* Edit form */}
      {editMode && (
        <form onSubmit={handleSave} className="edit-form">
          <h2>Uređivanje — {form.cpeId}</h2>

          <label htmlFor="ssid">SSID:</label>
          <input id="ssid" type="text" value={form.ssid}
            onChange={e => setForm({ ...form, ssid: e.target.value })} required maxLength={32} />

          <label htmlFor="wifiBand">WiFi pojas:</label>
          <select id="wifiBand" value={form.wifiBand}
            onChange={e => setForm({ ...form, wifiBand: e.target.value })}>
            {WIFI_BANDS.map(b => <option key={b.value} value={b.value}>{b.label}</option>)}
          </select>

          <label htmlFor="encryptionType">Enkripcija:</label>
          <select id="encryptionType" value={form.encryptionType}
            onChange={e => setForm({ ...form, encryptionType: e.target.value })}>
            {ENCRYPTION_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
          </select>

          {form.encryptionType !== 'OPEN' && (
            <>
              <label htmlFor="password">Lozinka:</label>
              <input id="password" type="text" value={form.password}
                onChange={e => setForm({ ...form, password: e.target.value })} minLength={8} maxLength={64} />
            </>
          )}

          <div className="btn-row">
            <button type="submit" disabled={loading}>
              {loading ? 'Spremanje...' : 'Spremi'}
            </button>
            <button type="button" className="btn-cancel" onClick={handleCancel}>Odustani</button>
          </div>
        </form>
      )}
    </div>
  )
}
