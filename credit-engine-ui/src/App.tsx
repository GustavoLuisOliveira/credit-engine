import {Outlet} from "react-router-dom";
import {ConfirmDialog} from "primereact/confirmdialog";


function App() {
  return (
    <>
      <main className="container">
        <Outlet/>
      </main>

      <ConfirmDialog/>
    </>
  )
}

export default App
