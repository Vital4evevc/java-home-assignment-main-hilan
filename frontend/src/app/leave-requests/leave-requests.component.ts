import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { LeaveRequestFormComponent } from '../leave-request-form/leave-request-form.component';

// NOTE: This component was written quickly for a POC.
// It talks to the API directly, manages state by hand and uses `any` everywhere.
@Component({
  selector: 'app-leave-requests',
  standalone: true,
  imports: [CommonModule, LeaveRequestFormComponent],
  templateUrl: './leave-requests.component.html',
  styleUrls: ['./leave-requests.component.css']
})
export class LeaveRequestsComponent implements OnInit {
  requests: any[] = [];
  loading = false;
  loadError = '';

  // Relative URL: the dev server proxies /api to the backend (see proxy.conf.json).
  private apiUrl = '/api/leave-requests';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.loadError = '';
    this.http.get<any>(this.apiUrl).subscribe({
      next: (data) => {
        this.requests = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.loadError = err.status === 0
          ? 'Could not reach the server at ' + this.apiUrl + '. Make sure the backend is running.'
          : 'Failed to load leave requests (HTTP ' + err.status + ').';
      }
    });
  }

  // Wired up by the candidate as part of the assignment.
  approve(id: number): void {
    // TODO (candidate): call POST /api/leave-requests/{id}/approve
    // and handle loading / error / success without a generic alert.
    this.http.post<any>(this.apiUrl + '/' + id + '/approve', {}).subscribe(() => {
      this.load();
    });
  }

  typeLabel(type: number): string {
    if (type == 0) return 'Vacation';
    if (type == 1) return 'Sick';
    return 'Unpaid';
  }

  statusLabel(status: number): string {
    if (status == 0) return 'Pending';
    if (status == 1) return 'Approved';
    return 'Rejected';
  }
}
