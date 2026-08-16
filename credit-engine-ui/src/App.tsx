import {Outlet} from "react-router-dom";
import {ConfirmDialog} from "primereact/confirmdialog";
import {Navbar} from "./components/shared/navbar/Navbar.tsx";


function App() {
    return (
        <>
            <Navbar />

            <main className="container my-2">
                <Outlet/>
            </main>

            <ConfirmDialog/>
        </>
    )
}

export default App
