import sys
from PyQt6.QtWidgets import QApplication
from ui.dashboard_app import DashboardApp

if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = DashboardApp()
    window.show()
    sys.exit(app.exec())